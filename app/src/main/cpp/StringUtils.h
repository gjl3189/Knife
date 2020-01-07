
//

#ifndef KNIFE_STRINGUTILS_H
#define KNIFE_STRINGUTILS_H

#include <string>
#include <vector>

std::string SubstringAfter(std::string &s, char delimiter);

std::string SubstringAfter(std::string &s, std::string &delimiter);

std::string SubstringAfterLast(std::string &s, char delimiter);

std::string SubstringAfterLast(std::string &s, std::string &delimiter);

std::string SubstringBefore(std::string &s, char delimiter);


std::string Repeat(std::string &s, int count);

std::string RemovePrefix(std::string &s, std::vector<std::string> &prefixs);

#endif //KNIFE_STRINGUTILS_H
