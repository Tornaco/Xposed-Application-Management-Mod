package github.tornaco.xposedmoduletest;

// oneway
interface IFileDescriptorConsumer {
    // IO.
    oneway void acceptAppParcelFileDescriptor(in ParcelFileDescriptor pfd);
}
