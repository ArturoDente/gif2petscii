# gif2petscii
*A tool to generate the Asm code for a monochrome petscii animation starting from a gif file*

## What it is ##
Gif to Petscii is a tool written in Java (I use version 11 in my envorinment, so probably you should have same version or above) that takes as input the path of a gif file and returns the asm code to copy paste into Cbm Prg Studio to compile and get the final .prg file ready to show the gif as a petscii animation.

Watch my [Star Wars tribute](https://youtu.be/MBeG2z_sKr4?t=5) or [Freddie Mercury tribute](https://youtu.be/mji0jbTy2w8?t=6)  videos to have an idea of the final result.

## What it is not ##
Gif to Petscii is not your replacement. It's intended for asm developers, even if the code will work you will want to edit it, to move memory in other locations, add sid tunes and whatever else.


## Usage ##
From the folder "target" download gif2petscii-1.0-SNAPSHOT.jar somewhere, open a command line on it and digit:

`java -jar Gif2Petscii.jar <options>`

where options are: 

`-s <source gif file>` ->the gif to convert (REQUIRED)

`c <number>` -> the number from where to start the labels for the petscii ram area in the asm code; it's useful if you want to combine more gifs together, starting from the ending of the previous one.

In this case, from the second gif asm code on, since the decompression code is already pasted from the first gif asm file, you will have to copy-paste only the `decodestream` calls (together with the others from the first gif) and the header/petsciis rows at the end.


## Final notes ##
For this tool I have implemented a simple RLE algorithm to compress the petsciis frames of the animation, but as for RLE specs, some pictures will not benefit from the crunching code (it can even get worse).

Anyway, compile the asm code and see the result, it should work good for gifs with not too many frames.

## Credits ##
I use [Petsciiator](https://github.com/EgonOlsen71/petsciiator) inside the tool to convert gif frames into petsciis.
