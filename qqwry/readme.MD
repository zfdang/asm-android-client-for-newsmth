QQIP地址数据库（纯真版）

the steps to update qqwry.dat:

1. download latest qqwry.dat from the following website:
	http://www.cz88.net/fox/ipdat.shtml

2. convert qqwry.dat to sqlite.db with scripts/sqlite_import.php
	project source: https://github.com/starfalling/php-IpLocationSeeker
	$ php sqlite_import.php
	qqwry.db will be generated.

	use tool http://www.codeproject.com/Articles/220018/SQLite-Compare-Utility to analyze the change if necessary

3. replace qqwry.db in /assets/databases/qqwry.zip

4. increase version in MyDatabase.java
	private static final int DATABASE_VERSION = 2;
