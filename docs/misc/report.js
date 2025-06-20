function getSearchParam(param) {
    return new URLSearchParams(window.location.search).get(param) || "";
};

// Put there required render functions to allow rendering of any object type.
function defaultRender (data, type, row, meta) {
// Default render supports:
// 1. String: just return the string (can be html).
// 2. Object with structure:
//   {
//      "title": "<html>",      # Tooltip of cell
//      "href": "<url>",        # Link in cell
//      "value": "string"       # Value of cell
//   }
    // console.log("defaultRenderer is called with type = " + type);
    if (type == 'sort') {
        if (data.sortByValue == '') return 0;
        return data.sortByValue;
    }

    // Be aligned with com.github.exadmin.ostm.uimodel.SeverityLevel
    // select chars at: https://www.compart.com/en/unicode/
    let value = data.value || '&nbsp;';
    if (data.severity == 'OK') value = value + '&nbsp;✅';
    if (data.severity == 'ERR') value = value + '&nbsp;❌';
    if (data.severity == 'WARN') value = value + '&nbsp;⛅';
    if (data.severity == 'INFO') value = value + '';
    if (data.severity == 'SEC') value = value + '&nbsp;&#128561;';
    if (data.severity == 'SKIP') value = value + '&nbsp;&#x1F4A4;';
    if (data.severity == 'PLACE1') value = value + '&nbsp;&#x1F947;';
    if (data.severity == 'INFO_PUB') value = value + '&nbsp;&#x1F513;'
    if (data.severity == 'INFO_PRIV') value = value + '&nbsp;&#x1F512;'
    if (data.severity == 'INFO_ARCH') value = value + '&nbsp;&#x1F4D5;'


    if (data.title && data.href) {
        return '<div title="<p class=\'my-tooltip\'>' + data.title + '</p>"><a href="' + data.href + '" target="_blank">' + value + '</a></div>';
    } else if (data.title) {
        return '<div title="' + data.title + '">' + value + '</div>';
    } else if (data.href) {
        return '<a href="' + data.href + '" target="_blank">' + value + '</a>';
    } else if (value) {
        return value;
    } else {
        return String(data);
    }
}

$(document).ready(function() {
    // Add custom sorting for 'only-numbers'
    jQuery.extend( jQuery.fn.dataTableExt.oSort, {
        "only-numbers-asc": function ( a,b ) {
            return a - b;
        },
        "only-numbers-desc": function ( a,b ) {
            return b - a;
        }
    } );

    $.getJSON('./data/data.json', function(data) {
        const reportName = data.reportName;
        const reportDate = data.reportDate;
        const tables = data.tables;

        // document.title = `Report ${reportName} on ${reportDate}`;
        // $('#report-name').text(`Report: ${reportName} generated at ${reportDate}`);

        let tabList = $('#tab-list');
        let tabsContainer = $('#tabs');

        tables.forEach((tableData, index) => {
            // Add tab
            tabList.append(`<li><a href="#tabs-${index + 1}">${tableData.title || index + 1}</a></li>`);

            // Add tab content with table
            tabsContainer.append(`<div id="tabs-${index + 1}"><table id="table-${index + 1}" class="display"></table></div>`);

            // Add help urls to column titles if exists
            tableData.columns.forEach(column => {
                if (column.help_url) {
                    column.title += `<a href="${column.help_url}" target="_blank" class="help-icon"><br>&#x1F56E;</a>`;
                }
                column.render = window[column.render] || defaultRender;
            });

            // Initialize DataTable
            $(`#table-${index + 1}`).DataTable({
                data: tableData.data,
                columns: tableData.columns,
                pageLength: -1,
                autoWidth: false,
                search: {
                    search: getSearchParam('search')
                }
            });
            $(`#table-${index + 1}`).on('search.dt', function syncSearches() {
                searchValue = $(this).DataTable().search();
                window.history.replaceState(null, null, `?search=${encodeURIComponent(searchValue)}` + window.location.hash);
                $.fn.dataTable.tables().forEach(function(table) {
                    if (table !== this) {
                        $(table).off('search.dt');
                        $('#'+table.id+'_filter input')[0].value = searchValue;
                        $(table).DataTable().search(searchValue).draw();
                        $(table).on('search.dt', syncSearches);
                    }
                });
            });
        });

        // Initialize tabs
        $("#tabs").tabs({
                activate: function(event, ui) {
                    window.location.hash = ui.newPanel.attr('id');
                }
            }
        );
    });

    $(function() {
      $(document).tooltip({
        content: function() {
          // Keep this code - to force JQueryUI render <br> as a new line!
          return $(this).prop('title');
        }
      });
    });

    $( function() {
        $( document ).tooltip({
          position: {
            my: "center bottom-20",
            at: "center top",
            using: function( position, feedback ) {
              $( this ).css( position );
              $( "<div>" )
                .addClass( "arrow" )
                .addClass( feedback.vertical )
                .addClass( feedback.horizontal )
                .appendTo( this );
            }
          }
        });
      } );

/*    $('[title]').tooltip({
        classes: {
            "ui-tooltip": "my-custom-tooltip"
        }
    });*/
});