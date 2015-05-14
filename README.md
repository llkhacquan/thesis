thesis
======

How to build and run this thesis with eclipse

1.	Build jpf-core and jpf-symbc first
	(Add to eclipse, Eclipse will build them automatically with Ant. Then you can remove these 1 projec (do not delete them from disk :lol:))
2. 	Add Verification Tool to eclipse and build (you can run without adding jpf-core and jpf-symbc prject to eclipse)
3.	Compile jj files with javacc
4.	Run the MainPage class

How to copy resources to run from jar

1.	Export MainPage run configuration to jar file
2. 	Copy resources, z3, jpf-core, jpf-symbc (built) to the same folder as jar file