to do list:
====
1. HttpClientDownloader
----
explanation: login / get post list and etc are using httpClient, but urlimageviewer are using httpurlconnection. so for images in newexpress, it might be rejected due to cookies issue. so it's better to use HTTPClient for urliamgeviewer as well. 
Action: add HttpClientDownloader, which is similar with HttpUrlDownloader

