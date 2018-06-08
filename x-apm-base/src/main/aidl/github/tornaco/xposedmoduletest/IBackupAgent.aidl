package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IBackupCallback;
import github.tornaco.xposedmoduletest.IFileDescriptorInitializer;

interface IBackupAgent {
   oneway void performBackup(in IFileDescriptorInitializer init, String domain, String path, in IBackupCallback callback);

   oneway void performRestore(in ParcelFileDescriptor pfd, String domain, String path, in IBackupCallback callback);
}
