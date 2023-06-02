# gif2petscii
*A tool to generate the Asm C64 code for a monochrome or colourful petscii animation starting from a gif file*

## What it is ##
Gif to Petscii is a tool written in Java (I use version 11 in my envorinment, so probably you should have same version or above) that takes as input the path of a gif file and returns the asm code of its petscii animation program. 

If acting as monochrome, the asm code has to be copied pasted into Cbm Prg Studio in order to compile it and to get the final .prg file ready to show the gif as a petscii animation.

If acting as colourful (using the switch -f colour in the command line), it generates a kickassembler code and a bunch of .bin files that are linked inside the code. You have to compile the asm code with kickassembler

Watch my [Star Wars tribute](https://youtu.be/MBeG2z_sKr4?t=5) or [Freddie Mercury tribute](https://youtu.be/mji0jbTy2w8?t=6)  videos to have an idea of the final result in monochromatic version.

Watch my [Star Wars tribubte 2023](https://youtu.be/qBbz1jloMTw) or [It's a Kind of Magic](https://youtu.be/36fXrDkj76A) videos to have an idea of the final result in multicolour version.

## What it is not ##
Gif to Petscii is not your replacement. It's intended for asm developers, even if the code will work you will want to edit it, to move memory in other locations, add sid tunes and whatever else.


## Usage ##
From the folder "target" download gif2petscii-1.0-SNAPSHOT.jar somewhere, open a command line on it and digit:

`java -jar Gif2Petscii.jar <options>`

where options are: 

`-s <source gif file>` ->the gif to convert (REQUIRED)

`-f colour` -> if you want the colourful conversion, not the monochromatic one.

`-c <number>` -> the number from where to start the labels for the petscii ram area in the asm code; it's useful if you want to combine more gifs together, starting from the ending of the previous one.

In this case, from the second gif asm code on, since the decompression code is already pasted from the first gif asm file, you will have to copy-paste only the `decodestream` calls (together with the others from the first gif) and the header/petsciis rows at the end.


## Final notes ##
For the monochromatic version I have implemented a simple RLE algorithm to compress the petsciis frames of the animation, but as for RLE specs, some pictures will not benefit from the crunching code (it can even get worse).

The colourful version uses Antonio Savona's tscrunch utility to compress and decompress data. It requires Windows because I embedded the tscrunch.exe file not having time to rewrite it from Python to Java. When using the tool, the .exe file is generated inside the folder where the gif2petscii jar tool is, so you don't have to download it manually.

Hint: use ezgif.com to optimize, reduce and eventually monochromize your gifs before using this program.

## Credits ##
I use [Petsciiator](https://github.com/EgonOlsen71/petsciiator) inside the tool to convert gif frames into petsciis.

I use [tscrunch](https://github.com/tonysavon/TSCrunch) from Antonio Savona embedding directly the .exe inside the .jar file.
