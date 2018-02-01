# thresholdBC
## Introduction
One of the problems of blockchain technology is private keys getting lost, this android application we developed aims to solve that problem. This application uses  [codehale](https://github.com/codahale/shamir) Shamir's secret sharing to divide a private key (or any secret) in k shares. Shares can be easily distributed among friends using NFC, more trustworthy friends can get multiple secrets.
A lost private key can be recovered by bringing friends together with at least n shares and combining their shares to recover the secret. This application functions independently from any particular blockchain, it can be used to make secure secret recovery possible for any kind of secret.

However, as an example an integration has been made in a fork https://github.com/TBruyn/trustchain_keyshare the trustchain mobile application https://github.com/wkmeijer/CS4160-trustchain-android .

## Getting Started

After cloning the project [gradle](https://gradle.org/) can be used to build the application. For debugging [android studio](https://developer.android.com/studio/index.html) is recommended 

## Contributing

When you encounter issues please create an issue on the [issue tracker](https://github.com/b-czaszynski/thresholdBC/issues). 


## Authors

See also the list of [contributors](https://github.com/b-czaszynski/thresholdBC/graphs/contributors) who participated in this project.


## Acknowledgments

* A slightly modified version (to make it compitable with java 7) of the java implementation of shamir secret sharing by codehale was used  (for java 7 compatibility) https://github.com/codahale/shamir
