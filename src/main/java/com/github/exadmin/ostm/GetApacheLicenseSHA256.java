package com.github.exadmin.ostm;

import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Paths;

public class GetApacheLicenseSHA256 {
    public static void main(String[] args) throws Exception {
        String licenseFileName = args[0];
        String body = FileUtils.readFile(Paths.get(licenseFileName));
        String modifiedBody = MiscUtils.getLettersOnly(body);
        String sha256 = MiscUtils.getSHA256AsBase64(modifiedBody);
        System.out.println("sha256 = " + sha256 + ", for the file = " + licenseFileName);
    }
}
