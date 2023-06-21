import gui.Notepad;

public class Main {
    public static void main(String[] args) {
        Notepad notepad = new Notepad();
        notepad.startWindow();
        if(args.length > 0) {
            notepad.readFile(args[0]);
        }
    }
}
