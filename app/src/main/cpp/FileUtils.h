#ifndef KNIFE_FILEUTILS_H#define KNIFE_FILEUTILS_H#ifdef __cplusplusextern "C" {#endif//#include <string>int DeleteDirectory(const char *path);int DeleteFileSystem(const char *path);int DirectoryExists(const char *path);//std::string ReadAllText(const char *path);// bool ListFiles(const char *path, bool(callback)(bool isDirectory, std::string &fullPath));#ifdef __cplusplus}#endif#endif //KNIFE_FILEUTILS_H