package gui;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Notepad {
    private final JFrame frame;
    private final JTextArea textArea;
    private final JLabel fontLabel;
    private String filePath;
    private final UndoManager undoManager;

    public Notepad() {
        FlatDarculaLaf.setup();

        frame = new JFrame();
        textArea = new JTextArea();
        fontLabel = new JLabel();
        filePath = "";
        undoManager = new UndoManager();
    }

    public void startWindow() {
        int borderMargin = 5;
        int margin = 10;

        //Ustawienie właściwości okna
        frame.setPreferredSize(new Dimension(900, 700));
        frame.setMinimumSize(new Dimension(400, 233));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Notepad - Untitled");

        //Wyśrodkowanie okna na ekranie
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 3);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 6);
        frame.setLocation(x, y);

        //Główny panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
//        mainPanel.setBorder(new EmptyBorder(20,20,20,20));
        frame.add(mainPanel, BorderLayout.CENTER);

        //Panel z menu głównym
        JPanel menuPanel = new JPanel();
//        menuPanel.setBorder(new EmptyBorder(20,20,20,20));
        menuPanel.setLayout(new BorderLayout());
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        //Menu główne
        JMenuBar menuBar = new JMenuBar();
        menuPanel.add(menuBar);
        frame.setJMenuBar(menuBar);

        //Elementy menu głównego
        borderMargin += 5;
        margin += 5;

        JMenu fileMenu = new JMenu("File");
        fileMenu.setBorder(new EmptyBorder(borderMargin, margin, borderMargin, margin));

        JMenu editMenu = new JMenu("Edit");
        editMenu.setBorder(new EmptyBorder(borderMargin, margin, borderMargin, margin));

        JMenu formatMenu = new JMenu("Format");
        formatMenu.setBorder(new EmptyBorder(borderMargin, margin, borderMargin, margin));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setBorder(new EmptyBorder(borderMargin, margin, borderMargin, margin));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);

        borderMargin -= 5;
        margin -= 5;

        //Podelementy File w menu
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save as");
        JMenuItem quitItem = new JMenuItem("Quit");

        //Dodanie listenerów
        newItem.addActionListener(e -> handleNew());
        openItem.addActionListener(e -> handleOpen());
        saveItem.addActionListener(e -> handleSave());
        saveAsItem.addActionListener(e -> handleSaveAs());
        quitItem.addActionListener(e -> quit());

        //Podelementy Edit w menu
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");

        //Dodanie listenerów
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });
        cutItem.addActionListener(e -> cutSelectedText());
        copyItem.addActionListener(e -> copySelectedTextToClipboard());
        pasteItem.addActionListener(e -> pasteFromClipboard());

        //Podelementy Format w menu
        JMenuItem fontItem = new JMenuItem("Font");
        JMenuItem fontSizeItem = new JMenuItem("Font size");
        JMenuItem fontColorItem = new JMenuItem("Font color");
        JMenuItem boldItem = new JMenuItem("Bold");
        JMenuItem italicItem = new JMenuItem("Italic");
        JMenuItem wordWrapItem = new JMenuItem("Word wrap");

        //Podelement Help w menu
        JMenu infoMenu = new JMenu("Info");

        //Podelementy Info w Help w menu
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem blogItem = new JMenuItem("My Linkedin");

        //Dodanie listenerów dla podelementów Format
        fontItem.addActionListener(e -> fontChange());
        fontSizeItem.addActionListener(e -> fontSizeChange());
        fontColorItem.addActionListener(e -> fontColorChange());
        boldItem.addActionListener(e -> setBoldFont());
        italicItem.addActionListener(e -> setItalicFont());
        wordWrapItem.addActionListener(e -> wordWrapChange());

        //Dodanie listenerów dla podelementów Info w Help
        aboutItem.addActionListener(e -> openAbout());
        blogItem.addActionListener(e -> openBlog());

        //Dodanie podelementów File do menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator(); // Dodaje separator między elementami menu
        fileMenu.add(quitItem);

        //Dodanie podelementów Edit do menu
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        //Dodanie podelementów Format do menu
        formatMenu.add(fontItem);
        formatMenu.add(fontSizeItem);
        formatMenu.add(fontColorItem);
        formatMenu.addSeparator();
        formatMenu.add(boldItem);
        formatMenu.add(italicItem);
        formatMenu.addSeparator();
        formatMenu.add(wordWrapItem);

        //Dodanie podelementu Help do menu
        helpMenu.add(infoMenu);

        //Dodanie podelementów Info
        infoMenu.add(aboutItem);
        infoMenu.add(blogItem);

        //Panel z polem tekstowym
        JPanel textPanel = new JPanel();
        textPanel.setBorder(new EmptyBorder(0, margin + 5, 0, margin + 5));
        textPanel.setLayout(new BorderLayout());
        mainPanel.add(textPanel, BorderLayout.CENTER);

        //Pole tekstowe, które pokazuje zawartość pliku
        textArea.setFont(new Font("Calibri", Font.PLAIN, 16));
        textArea.setBorder(new EmptyBorder(margin, margin, margin, margin));
        textPanel.add(textArea, BorderLayout.CENTER);

        //Dodanie listenerów klawiszy do pola tekstowego
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    handleSave();
                } else if (e.isControlDown() && e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    handleSaveAs();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_O && !e.isAltDown()) {
                    handleOpen();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N && !e.isAltDown()) {
                    handleNew();
                } else if (e.isAltDown() && !e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    wordWrapChange();
                } else if (e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } else if (e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } else if(e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_B) {
                    setBoldFont();
                } else if(e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_I) {
                    setItalicFont();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        //Dodanie listenera edycji do pola tekstowego
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        //Pasek przewijania po tekście
        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        //Panel z etykietą pokazującą informacje o czcionce
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(new EmptyBorder(7, margin + 5, 3, 0));
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        //Etykieta pokazująca informacje o czcionce
        fontLabel.setText("Calibri 16");
        fontLabel.setFont(new Font("Calibri", Font.PLAIN, 15));
        infoPanel.add(fontLabel);

        frame.pack();
        frame.setVisible(true);
    }

    //Utworzenie nowego pliku, wyczyszczenie zawartości pola tekstowego
    private void handleNew() {
        if (!textArea.getText().equals("")) {
            //Pole tekstowe nie jest puste, pytamy się uzytkownika, czy na pewno chce utworzyć nowy plik
            int answer = JOptionPane.showOptionDialog(
                    null,
                    "Are you sure you want to create a new file? Any unsaved changes will be lost.",
                    "New file",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null
            );

            if (answer == JOptionPane.YES_OPTION) {
                textArea.setText("");
                filePath = "";

                frame.setTitle("Notepad - Untitled");
            }
        } else {
            frame.setTitle("Notepad - Untitled");
            filePath = "";
        }
    }

    //Otwarcie menu dialogowego z wyborem pliku do odczytu
    private void handleOpen() {
        FileDialog fileDialog = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
        fileDialog.setDirectory("%userprofile%");
        fileDialog.setVisible(true);

        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            //Pole tekstowe nie jest puste, wygenerowanie menu dialogowego z zapytaniem
            if (!textArea.getText().equals("")) {
                int answer = JOptionPane.showOptionDialog(
                        null,
                        "Are you sure you want to open this file? Any unsaved changes will be lost.",
                        "Open a file",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null
                );

                //Użytkownik potwierdził chęć otwarcia tego pliku, otwarcie i odczytanie wybranego pliku
                if (answer == JOptionPane.YES_OPTION) {
                    readFile(fileDialog.getDirectory() + fileDialog.getFile());
                }
            } else {
                //Pole tekstowe jest puste, więc od razu odczytujemy plik
                readFile(fileDialog.getDirectory() + fileDialog.getFile());
            }
        }
    }

    //Odczytanie otwartego pliku
    public void readFile(String path) {
        filePath = path;

        try (FileReader fileReader = new FileReader(path);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            textArea.setText("");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                textArea.setText(textArea.getText() + line + "\n");
            }

            //Wpisanie nazwy pliku do tytułu okna
            File file = new File(path);
            frame.setTitle("Notepad - " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "An error occurred while opening the file", "Error", JOptionPane.ERROR_MESSAGE);
            filePath = "";
            frame.setTitle("Notepad - Untitled");
        }
    }

    //Rozpoczęcie procedury Zapisz jako
    private void handleSaveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(700, 450));
        int option = fileChooser.showSaveDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getSelectedFile().getAbsolutePath();
            saveToFile();
        }
    }

    //Rozpoczęcie procedury Zapisz
    private void handleSave() {
        if (filePath.equals("")) {
            handleSaveAs();
        } else {
            saveToFile();
        }
    }

    //Zapis pliku
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(textArea.getText());
            writer.close();
            JOptionPane.showMessageDialog(frame, "The file has been saved", "File saved", JOptionPane.INFORMATION_MESSAGE);

            File file = new File(filePath);
            frame.setTitle("Notepad - " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "An error occurred while saving the file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Zawijanie wierszy
    private void wordWrapChange() {
        if (textArea.getLineWrap() && textArea.getWrapStyleWord()) {
            textArea.setLineWrap(false);
            textArea.setWrapStyleWord(false);
        } else {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
        }
    }

    //Zmiana czcionki
    private void fontChange() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        JComboBox<String> fontComboBox = new JComboBox<>(fontNames);

        // Wyświetlenie dialogu z wyborem czcionki
        int result = JOptionPane.showOptionDialog(
                frame,
                fontComboBox,
                "Choose a font",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );

        // Jeśli wybrano OK, ustawienie wybranej czcionki dla pola tekstowego
        if (result == JOptionPane.OK_OPTION) {
            String selectedFontName = (String) fontComboBox.getSelectedItem();
            Font selectedFont = new Font(selectedFontName, Font.PLAIN, textArea.getFont().getSize());
            textArea.setFont(selectedFont);

            //Zmiana etykiety z informacjami o czcionce
            updateFontLabel(selectedFontName + " " + textArea.getFont().getSize());
        }
    }

    //Zmiana rozmiaru czcionki
    private void fontSizeChange() {
        String selectedSize = JOptionPane.showInputDialog(
                frame,
                "Enter a font size:",
                "Font size",
                JOptionPane.INFORMATION_MESSAGE
        );
        if (selectedSize != null && !selectedSize.equals("")) {
            int fontSize = Integer.parseInt(selectedSize);

            // Ustawienie wybranego rozmiaru czcionki dla pola tekstowego
            Font currentFont = textArea.getFont();
            Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), fontSize);
            textArea.setFont(newFont);

            //Zmiana etykiety z informacjami o czcionce
            updateFontLabel(currentFont.getName() + " " + fontSize);
        }
    }

    //Zmiana koloru czcionki
    private void fontColorChange() {
        Color selectedColor = JColorChooser.showDialog(frame, "Font color", textArea.getForeground());

        // Ustawienie wybranego koloru czcionki dla pola tekstowego
        textArea.setForeground(selectedColor);
    }

    //Ustawienie pochylenia czcionki
    private void setItalicFont() {
        String fontName = textArea.getFont().getName();
        int fontSize = textArea.getFont().getSize();

        if(!textArea.getFont().isItalic()) {
            textArea.setFont(new Font(fontName, Font.ITALIC, fontSize));
        } else {
            textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));
        }
    }

    //Ustawienie pogrubienia czcionki
    private void setBoldFont() {
        String fontName = textArea.getFont().getName();
        int fontSize = textArea.getFont().getSize();

        if(!textArea.getFont().isBold()) {
            textArea.setFont(new Font(fontName, Font.BOLD, fontSize));
        } else {
            textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));
        }
    }

    //Zmiana etykiety z informacjami o czcionce
    private void updateFontLabel(String content) {
        fontLabel.setText(content);
    }

    //Otwarcie strony profilu na Linkedin
    private void openBlog() {
        String url = "https://www.linkedin.com/in/jakub-ma%C5%84ko-7b3a47266/";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(frame, "The website could not be opened", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Otwarcie okienka z kodem QR
    private void openAbout() {
        JFrame aboutFrame = new JFrame("Notepad - QR code");
        aboutFrame.setPreferredSize(new Dimension(400, 400));
        aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        aboutFrame.setResizable(false);

        ImageIcon icon = new ImageIcon("frame.png");
        JLabel label = new JLabel(icon);

        aboutFrame.getContentPane().add(label);
        aboutFrame.pack();
        aboutFrame.setLocationRelativeTo(null);
        aboutFrame.setVisible(true);
    }

    //Skopiowanie zaznaczonego tekstu do schowka
    private void copySelectedTextToClipboard() {
        String selectedText = textArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            StringSelection selection = new StringSelection(selectedText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    //Wycięcie zaznaczonego tekstu
    private void cutSelectedText() {
        int start = textArea.getSelectionStart();
        int end = textArea.getSelectionEnd();

        if (start != -1 && end != -1) {
            String selectedText = textArea.getSelectedText();
            textArea.replaceRange("", start, end);
            textArea.setCaretPosition(start);
            textArea.requestFocusInWindow();
            setClipboardContents(selectedText);
        }
    }

    //Ustawienie zawartości schowka
    private void setClipboardContents(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    //Wklejenie tekstu ze schowka
    private void pasteFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String clipboardData = "";

        try {
            clipboardData = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            JOptionPane.showMessageDialog(frame, "An error occurred while pasting the text", "Error", JOptionPane.ERROR_MESSAGE);
        }

        int caretPosition = textArea.getCaretPosition();
        textArea.insert(clipboardData, caretPosition);
    }

    //Wyjście
    private void quit() {
        if (textArea.getText().equals("")) {
            //Pole tekstowe jest puste, więc od razu zamykamy aplikację
            frame.dispose();
        } else {
            //Pole tekstowe nie jest puste, więc pytamy użytkownika, czy na pewno chce zamknąć aplikację
            int answer = JOptionPane.showOptionDialog(
                    null,
                    "Are you sure you want to quit? Any unsaved changes will be lost.",
                    "Quit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null
            );

            if (answer == JOptionPane.YES_OPTION) {
                frame.dispose();
            }
        }
    }
}
