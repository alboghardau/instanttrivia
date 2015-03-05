# Java Tomcat Connector
# apt-get install -y libapache2-mod-jk

# NodeJs Connector
# Hosting NodeJs
# http://stackoverflow.com/questions/14369865/running-node-js-in-apache

# Perl Connector
# apt-get install -y libapache2-mod-perl2

#PHP Connector
# apt-get install -y libapache2-mod-php5
# apt-get install -y libapache2-mod-fastcgi libapache2-mod-fcgid
# @TODO: What is libapache2-mod-php5filter ?

# Python Connector
# apt-get install -y libapache2-mod-python libapache2-mod-wsgi

# Rails Connector
# apt-get install -y libapache2-mod-passenger

# utils.info "Enabling modules: rewrite proxy proxy_fcgi proxy_fdpass proxy_html proxy_http xml2enc ..."
# os.pkmgr.is_apt && a2enmod rewrite proxy proxy_fcgi proxy_fdpass proxy_html proxy_http xml2enc && service apache2 restart
# utils.info "done."

















# Project Jupiter

Named by the [Jupiter](http://en.wikipedia.org/wiki/Moons_of_Jupiter), this project should save and boost the way [Avadon One](http://itmediaconnect.ro)'s servers are managed.

## Virtualization &amp; Operating Systems

During time, Jupiter project has changed, evolving from an idea of a set of independent Root servers, to a set of independent linux containers (or other virtual machines) hosted on one or multiple Root servers.

Further more, the project would have a physical layer, which would be form of twin Root Servers ... (@TODO: extend...)

Unfortunately the solutions we use cannot be set on only one distribution, due to the specificity of certain solutions (i.e. FreeIPA is specific to Fedora/Redhat based solutions) so we chose a set of distributions we would use further.

### Ubuntu X.04 LTS

We choose Ubuntu Linux over Debian due to Debian's licensing issues which will probably create a lot of problems in time.

We will always use LTS versions (see 12.04, 14.04, etc) which are longer supported by Canonical and easier to maintain for us.

### Unbreakable Kernel

Oracle Unbreakable Kernel is our second choice. We chose Oracle Linux in stead of the original (Redhat Linux) due to cheaper support in case we require higher levels of maintenance. This servers will hold some our our more sensitive tools, like:

* FreeIPA user manager
* OpenVPN tools
* @TODO: Other Vital Systems

### CentOS

Choosing CentOS as a 3rd OS came from the fact that both CPanel and Plesk are written for this distro mainly. No intentions in using it soon. Our preference may even change in time.

## Server Deployment &amp; Install

Candidates:

* https://chef.io
* https://puppetlabs.com
* http://saltstack.com

## Servers

### Root Server :: Jupiter

* CPU: **AMD64** 6 + 6 Cores
* RAM: **32 G** (16 At start, will extend)
* HDD: __4 * 2 Tb in RAID10__ ( For data )
* SSD: __2 * 128 Gb in RAID10__ (ONLY for Jupiter OS (MAYBE! for containers))
* NET: 2 Intel Network Cards
* @TODO: What else?



#### Logstash & Elastic Search

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Varnish

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Syslog-NG

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

### Services :: Io
* FreeIPA OpenVPN

### "NFS" :: Ganymede

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

### Http/Loadbalancer Server :: Metis

#### Http Server :: Metis [(Apache)](http://apache.org)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Metis-[E(xpress)](http://expressjs.com) : 8081 (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Metis-[N(ginx)](http://nginx.org) : 8082 (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Metis-[W(ildfly)](http://wildfly.org) : 8083 (LOW PRIORITY)

See also: http://jboss.org

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

### Database Server :: Themisto (MySQL)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Themisto-Mo(ngoDb) (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Themisto-Pg(SQL) (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Themisto-Sq(Lite) (IRELEVANT)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

### Postfix Server :: Adrastea

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

### Develop Server :: Amalthea (PHP)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Amalthea-Ja(va) (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Amalthea-Js (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.

#### Amalthea-Py (LOW PRIORITY)

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse mollis turpis vitae arcu sodales interdum. Nullam vitae aliquam est. Phasellus sodales, libero at convallis aliquet, nibh orci ullamcorper elit, id feugiat dui tellus at elit. Nunc hendrerit eu sapien in molestie. Phasellus aliquam ac enim id tempor. Vestibulum elit leo, convallis eget felis ac, posuere convallis lorem. Suspendisse lacus felis, laoreet at cursus at, faucibus nec turpis. Sed nisl nibh, sagittis in suscipit nec, dictum vitae velit. Etiam ornare nisl sed pellentesque dignissim. Quisque lectus est, suscipit a malesuada vel, tempor id lacus.
