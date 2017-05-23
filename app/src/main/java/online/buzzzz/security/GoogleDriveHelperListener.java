package online.buzzzz.security;

public interface GoogleDriveHelperListener {
    void onSuccessfulConnection();
    void onSuccessfulFileOpened(String fileContents);
    void onSuccessfulFileSaved();
    void onConnectionFailure();
    void onFileOpenFailure();
    void onFileSaveFailure();
    void onSuccessfulFileCreated();
    void onFileCreateFailure();
}
