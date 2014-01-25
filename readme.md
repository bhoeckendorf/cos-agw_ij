COS AGW ImageJ plugin bundle
============================


## Documentation ##

Detailed documentation is available on the [Wiki](https://github.com/bhoeckendorf/cos-agw_ij/wiki).


## Download ##

  - [![Build Status](https://buildhive.cloudbees.com/job/bhoeckendorf/job/cos-agw_ij/badge/icon)](https://buildhive.cloudbees.com/job/bhoeckendorf/job/cos-agw_ij/)
  
  - Last successful build: [cos-agw_ij--SNAPSHOT.jar](https://buildhive.cloudbees.com/job/bhoeckendorf/job/cos-agw_ij/ws/build/libs/cos-agw_ij--SNAPSHOT.jar)


## Included plugins ##

  - DoG Filter  
    A scale-space filter.

  - Edit Regions  
    An editor for connected components.

  - Find Intensity Centers  
    Finds centers of blobs of identical intensity, such as yielded by a connected components analysis.

  - Logarithmic 8bit  
    Logarithmic scaling of images with higher dynamic range to 8bit.

  - Make Isotropic  
    Scales an image or a volume to isotropic sampling using bicubic interpolation.

  - Map Project  
    Projects a 3D spherical object onto a single 2D plane like a world map.  
    Supports performing multiple concentric projections onto a stack of planes to retain 3D spatial information.

  - Orthogonal Project  
    Fast and memory-friendly maximum intensity projections along X and Y dimensions without turning the volume.
    
  - Unlock Image  
    Convenience plugin to unlock images that are stuck in a locked state.


## Dependencies ##

  - [ImageJ](http://http://rsbweb.nih.gov/ij) or [Fiji](http://fiji.sc)  
    Fiji is recommended.

  - [ImgLib2](http://fiji.sc/wiki/index.php/ImgLib2)  
    Please note that ImgLib2 is part of the Fiji distribution of ImageJ.

  - [Java 3D](http://java3d.java.net/binary-builds.html)  
    Please note that Java 3D is already part of the standard Fiji + JRE bundle
    available [here](http://fiji.sc/wiki/index.php/Downloads).


----------

The COS AGW ImageJ plugin bundle is an open source project hosted on GitHub:
https://github.com/bhoeckendorf/cos-agw_ij

It is licensed under the [MIT License](http://mit-license.org).
See license.txt for details.