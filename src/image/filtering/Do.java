/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package image.filtering;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Slider;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jdk.nashorn.internal.parser.Token;
import jdk.nashorn.internal.parser.TokenType;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Habbab
 */


public class Do {

    static class Kernel {

        // Blur filter 
        public final static int[][] BLUR = new int[][]{
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };

        // gaussian filter 
        public final static int[][] GAUSSIAN = new int[][]{
            {0, 1, 0},
            {1, 4, 1},
            {0, 1, 0}
        };

        // sharpen filter
        public final static int[][] SHARPEN = new int[][]{
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
        };

        // edge horizontal
        public final static int[][] EDGE_HOR = new int[][]{
            {0, -1, 0},
            {0, 1, 0},
            {0, 0, 0}
        };

        // edge vertocal
        public final static int[][] EDGE_VER = new int[][]{
            {0, 0, 0},
            {-1, 1, 0},
            {0, 0, 0}
        };

        // edge digonal
        public final static int[][] EDGE_DIA = new int[][]{
            {-1, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
        };

        // edge detection
        public final static int[][] EDGE_ALL = new int[][]{
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}

        };

        // emboss east
        public final static int[][] EMBOSS_EAST = new int[][]{
            {-1, 0, 1},
            {-1, 1, 1},
            {-1, 0, 1}
        };

        // emboss south
        public final static int[][] EMBOSS_SOUTH = new int[][]{
            {-1, -1, -1},
            {0, 1, 0},
            {1, 1, 1}
        };

        // emboss south-east
        public final static int[][] EMBOSS_SOUTH_EAST = new int[][]{
            {-1, -1, 0},
            {-1, 1, 1},
            {0, 1, 1}
        };

    }

    enum filter {
        BLUR,
        GAUSSIAN,
        SHARPEN,
        EDGE_HOR,
        EDGE_VER,
        EDGE_DIA,
        EDGE_ALL,
        EMBOSS_EAST,
        EMBOSS_SOUTH,
        EMBOSS_SOUTH_EAST
    }

    public static JFrame frame;

    public static JPanel panel;
    static public BufferedImage image;
    static public BufferedImage originalImage;
    static public BufferedImage imageOriginal;
    static public JLabel label;
    static public JButton DDA = new JButton("DDA");
    static public JButton midPointCircle = new JButton("Circle");
    static public JButton antiAliased = new JButton("Anti aliased line");
    static public JTextField thicknessField = new JTextField("1");
    static public JButton superSampling = new JButton("Super sampling");
    static public JButton clipping = new JButton("clipping");
    static public JButton rectangle = new JButton("draw rectangle");
    static public JButton edgeMake = new JButton("make edge");
    static public JButton closePolygon = new JButton("close polygon");
    static public JButton fillPolygon = new JButton("fill polygon");
    public Do() {
        frame = new JFrame();
        frame.setSize(600, 600);
        frame.setVisible(true);
        panel = new JPanel(new MigLayout());
        label = new JLabel();
        panel.add(label, "span 4 4");
        frame.add(panel);
        makeButtons();
        frame.pack();

    }

    private void makeButtons() {
        JButton open = new JButton("choose an image");
        JButton inversion = new JButton("Inversion");
        JSlider gammaCorrection = new JSlider(JSlider.HORIZONTAL, 1, 15, 3);
        JSlider brightness = new JSlider(JSlider.HORIZONTAL, -255, 255, 0);
        JSlider contrastEnhancement = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
        JButton blur = new JButton("Blur");
        JButton gaussian = new JButton("Gaussian");
        JButton sharpen = new JButton("Sharpen");
        JButton edgeHor = new JButton("Edge horizontal");
        JButton edgeVer = new JButton("Edge vertical");
        JButton edgeDia = new JButton("Edge diagonal");
        JButton edgeAll = new JButton("Edge detection");
        JButton embossEast = new JButton("Emboss east");
        JButton embossSouth = new JButton("Emboss south");
        JButton embossSouthEast = new JButton("Emboss south-east");
        JButton original = new JButton("Original");
        // choose an image
        {

            open.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JFileChooser chooser;
                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Choose a directory");

                    //
                    // disable the "All files" option.
                    //
                    chooser.setAcceptAllFileFilterUsed(false);
                    // get currentDirectory and check if it has only images

                    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {

                        try {
                            image = ImageIO.read(chooser.getSelectedFile());
                            originalImage = ImageIO.read(chooser.getSelectedFile());
                            imageOriginal = ImageIO.read(chooser.getSelectedFile());
                            label.setIcon(new ImageIcon(image));
                            frame.pack();
                        } catch (IOException ex) {
                            Logger.getLogger(Do.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("No Selection ");
                    }
                }
            });

        }

        // Inversion
        {
            inversion.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < image.getHeight(); ++i) {
                        for (int j = 0; j < image.getWidth(); j++) {
                            Color color = new Color(image.getRGB(j, i));
                            Color newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
                            image.setRGB(j, i, newColor.getRGB());
                        }
                    }
                    label.updateUI();
                }
            });

        }

        // gamma correction
        {
            gammaCorrection.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    int add = ((JSlider) ce.getSource()).getValue();
                    double gamma = add / 3.0;

                    for (int i = 0; i < image.getHeight(); ++i) {
                        for (int j = 0; j < image.getWidth(); j++) {
                            Color color = new Color(originalImage.getRGB(j, i));
                            int newRed = (int) (Math.pow((color.getRed() / 255.0), gamma) * 255.0);
                            int newGreen = (int) (Math.pow((color.getGreen() / 255.0), gamma) * 255.0);
                            int newBlue = (int) (Math.pow((color.getBlue() / 255.0), gamma) * 255.0);
                            Color cc = new Color(newRed, newGreen, newBlue);
                            image.setRGB(j, i, cc.getRGB());
                        }
                    }
                    label.updateUI();
                }
            });
        }

        // brightness
        {
            brightness.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    int add = ((JSlider) ce.getSource()).getValue();
                    for (int i = 0; i < image.getHeight(); ++i) {
                        for (int j = 0; j < image.getWidth(); j++) {
                            Color color = new Color(originalImage.getRGB(j, i));
                            int newRed = Math.max(0, Math.min(color.getRed() + add, 255));
                            int newGreen = Math.max(0, Math.min(color.getGreen() + add, 255));
                            int newBlue = Math.max(0, Math.min(color.getBlue() + add, 255));
                            Color cc = new Color(newRed, newGreen, newBlue);
                            image.setRGB(j, i, cc.getRGB());
                        }
                    }
                    label.updateUI();
                }
            });
        }

        // contrast enhancement
        {

            contrastEnhancement.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    int add = ((JSlider) ce.getSource()).getValue();

                    for (int i = 0; i < image.getHeight(); ++i) {
                        for (int j = 0; j < image.getWidth(); j++) {
                            Color color = new Color(originalImage.getRGB(j, i));
                            int newRed = Math.min(255, Math.max(0, (int) ((color.getRed() - add) * (127.0 / (127.0 - add)))));
                            int newGreen = Math.min(255, Math.max(0, (int) ((color.getGreen() - add) * (127.0 / (127.0 - add)))));
                            int newBlue = Math.min(255, Math.max(0, (int) ((color.getBlue() - add) * (127.0 / (127.0 - add)))));
                            Color cc = new Color(newRed, newGreen, newBlue);
                            image.setRGB(j, i, cc.getRGB());
                        }
                    }
                    label.updateUI();
                }
            });
        }

        // blur 
        {
            blur.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.BLUR);
                }
            });
        }

        // gaussian
        {
            gaussian.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.GAUSSIAN);
                }
            });
        }

        // sharpen
        {
            sharpen.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.SHARPEN);
                }
            });
        }

        // edge horizontal
        {
            edgeHor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EDGE_HOR);
                }
            });
        }

        // edge vertical
        {
            edgeVer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EDGE_VER);
                }
            });
        }

        // edge diagonal
        {
            edgeDia.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EDGE_DIA);
                }
            });
        }

        // edge detecion
        {
            edgeAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EDGE_ALL);
                }
            });
        }

        // emboss east
        {
            embossEast.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EMBOSS_EAST);
                }
            });
        }

        // emboss south
        {
            embossSouth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EMBOSS_SOUTH);
                }
            });
        }

        // emboss south-east
        {
            embossSouthEast.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    convolution(filter.EMBOSS_SOUTH_EAST);
                }
            });
        }

        // original
        {
            original.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < imageOriginal.getHeight(); i++) {
                        for (int j = 0; j < imageOriginal.getWidth(); j++) {
                            originalImage.setRGB(j, i, imageOriginal.getRGB(j, i));
                            image.setRGB(j, i, imageOriginal.getRGB(j, i));
                        }
                    }
                    label.updateUI();
                }
            });
        }

       
        
        // group buttons
        {
            panel.add(open);
            panel.add(inversion, "wrap");

            JLabel brightnessLabel = new JLabel("Brightness");
            JPanel brightnessPanel = new JPanel(new MigLayout());
            brightnessPanel.add(brightnessLabel, "wrap");
            brightnessPanel.add(brightness);
            panel.add(brightnessPanel, "wrap");

            JLabel gammaLabel = new JLabel("Gamma correction");
            JPanel gammaPanel = new JPanel(new MigLayout());
            gammaPanel.add(gammaLabel, "wrap");
            gammaPanel.add(gammaCorrection);
            panel.add(gammaPanel, "wrap");

            JPanel cont = new JPanel(new MigLayout());
            JLabel ll = new JLabel("contrast");
            cont.add(ll, "wrap");
            cont.add(contrastEnhancement);
            panel.add(cont, "wrap");
            panel.add(blur);
            panel.add(gaussian);
            panel.add(original, "wrap");
            panel.add(sharpen);
            panel.add(edgeHor);
            panel.add(edgeVer);
            panel.add(edgeDia);
            panel.add(edgeAll);
            panel.add(embossEast);
            panel.add(embossSouth);
            panel.add(embossSouthEast, "wrap");
            panel.add(DDA);
            panel.add(midPointCircle);
            panel.add(antiAliased);
            panel.add(thicknessField);
            panel.add(superSampling, "wrap");
            panel.add(clipping);
            panel.add(rectangle);
            panel.add(edgeMake);
            panel.add(closePolygon);
            panel.add(fillPolygon);
//            panel.add(contrastEnhancement);
        }
    }

    public void convolution(filter par) {
        int[][] now = new int[3][3];
        if (par == filter.BLUR) {
            now = Kernel.BLUR;
        }
        if (par == filter.GAUSSIAN) {
            now = Kernel.GAUSSIAN;
        }
        if (par == filter.SHARPEN) {
            now = Kernel.SHARPEN;
        }
        if (par == filter.EDGE_HOR) {
            now = Kernel.EDGE_HOR;
        }
        if (par == filter.EDGE_VER) {
            now = Kernel.EDGE_VER;
        }
        if (par == filter.EDGE_DIA) {
            now = Kernel.EDGE_DIA;
        }
        if (par == filter.EDGE_ALL) {
            now = Kernel.EDGE_ALL;
        }
        if (par == filter.EMBOSS_EAST) {
            now = Kernel.EMBOSS_EAST;
        }
        if (par == filter.EMBOSS_SOUTH) {
            now = Kernel.EMBOSS_SOUTH;
        }
        if (par == filter.EMBOSS_SOUTH_EAST) {
            now = Kernel.EMBOSS_SOUTH_EAST;
        }
        for (int i = 0; i < image.getHeight() - 2; ++i) {
            for (int j = 0; j < image.getWidth() - 2; j++) {

                int[][] matRed = new int[3][3];
                int[][] matGreen = new int[3][3];
                int[][] matBlue = new int[3][3];
                for (int a = 0; a < 3; a++) {
                    for (int b = 0; b < 3; b++) {
                        Color color = new Color(originalImage.getRGB(j + b, i + a));
                        matRed[a][b] = color.getRed();
                        matGreen[a][b] = color.getGreen();
                        matBlue[a][b] = color.getBlue();
                    }
                }

                int newRed = Math.max(0, Math.min(255, (multiplication(now, matRed))));
                int newGreen = Math.max(0, Math.min(255, (multiplication(now, matGreen))));
                int newBlue = Math.max(0, Math.min(255, (multiplication(now, matBlue))));

                Color newColor = new Color(newRed, newGreen, newBlue);
                image.setRGB(j + 1, i + 1, newColor.getRGB());
            }
        }
        label.updateUI();
    }

    public int multiplication(int[][] a, int[][] b) {

        int sum = 0;
        int divisor = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; ++j) {
                sum += a[i][j] * b[i][j];
                divisor += a[i][j];
            }
        }
        if (divisor == 0) {
            divisor++;
        }
        return (int) ((1.0 * sum) / divisor);
    }
}
