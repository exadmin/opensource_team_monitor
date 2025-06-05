package com.github.exadmin.ostm;

import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;

public class GetApacheLicenseSHA256 {
    public static void main(String[] args) throws Exception {
        String licenseFileName = args[0];
        String body = FileUtils.readFile(licenseFileName);
        String modifiedBody = MiscUtils.getLettersOnly(body);
        String sha256 = MiscUtils.getSHA256FromString(modifiedBody);
        System.out.println("sha256 = " + sha256 + ", for the file = " + licenseFileName);
    }
}
