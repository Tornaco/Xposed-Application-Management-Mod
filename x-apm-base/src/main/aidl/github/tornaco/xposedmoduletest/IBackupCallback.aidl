package github.tornaco.xposedmoduletest;

interface IBackupCallback {
   oneway void onBackupFinished(String domain, String path);
   oneway void onRestoreFinished(String domain, String path);
   oneway void onFail(String message);
   oneway void onProgress(String progressMessage);
}
