To utilise Icon Evaluate, create a folder called "icons" in the source directory or alongside the executable jar, and place icons within it as .png, .jpg, or .jpeg files.

Once the program has finished execution, a file called "Similarity Report.txt" will be output in the source directory or alongside the executable jar.
If the program is run again, the new results will be appended to this file.

This program requires an installation of Open CV 4.5.5, with opencv-455.jar added as a library to the project. When compiled, opencv_java455.dll must be included alongside the jar file (this is included in \out\artifacts\Icon_Evaluate_jar\). Only Windows is officially supported.
