package ninja.majewski.springbootsharepointrest.dropbox;

@FunctionalInterface
interface DropboxActionResolver<T> {

    T perform() throws Exception;

}
