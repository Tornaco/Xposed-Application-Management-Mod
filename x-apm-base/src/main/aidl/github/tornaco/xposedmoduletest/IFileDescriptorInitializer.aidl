package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.IFileDescriptorConsumer;

// oneway
interface IFileDescriptorInitializer {
    oneway void initParcelFileDescriptor(String domain, String path, in IFileDescriptorConsumer consumer);
}
