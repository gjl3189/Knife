package euphoria.psycho.knife.download;

public interface DownloadObserver {


    void updateStatus(DownloadInfo downloadInfo);

    void updateProgress(DownloadInfo downloadInfo);



    void retried(DownloadInfo downloadInfo);

    void deleted(DownloadInfo downloadInfo);
}
