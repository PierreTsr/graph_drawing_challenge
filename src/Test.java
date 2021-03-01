import processing.core.PApplet;

public class Test {

	public static void main(String[] args) {
		System.out.println("Tools for the \"Graph Drawing Contest 2021: Live Challenge\"");
		if(args.length<1) {
			System.out.println("Error: one argument required: input file in JSON format");
			System.exit(0);
		}

		/** input file storing the instance of the problem */
		String inputFile;
		
		inputFile=args[0];
		System.out.println("Input file: "+inputFile);
		
		if(inputFile.endsWith(".json")==false) {
			System.out.println("Error: wrong input format");
			System.out.println("Supported input format: JSON format");
			System.exit(0);
		}

		GridLayout layout=IO.loadInputFromJSON(inputFile); // read the input file (problem instance)
	}

}
