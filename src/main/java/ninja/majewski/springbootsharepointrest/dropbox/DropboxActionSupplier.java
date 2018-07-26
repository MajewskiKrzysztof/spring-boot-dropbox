package ninja.majewski.springbootsharepointrest.dropbox;

@FunctionalInterface
interface DropboxActionSupplier<T> {

    T get() throws Exception;

}
