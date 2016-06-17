package me.tatarka.holdr.gradle

class PackageNameFinder {

    static String packageName(File manifestSrc) {
        try {
            return findPackageNameNew(manifestSrc)
        } catch (Exception e) {
            return findPackageNameOld(manifestSrc)
        }
    }

    private static String findPackageNameNew(File manifestSrc) {
        return new com.android.builder.core.DefaultManifestParser(manifestSrc).package
    }

    private static String findPackageNameOld(File manifestSrc) {
        return com.android.builder.core.VariantConfiguration.getManifestPackage(manifestSrc)
    }
}
