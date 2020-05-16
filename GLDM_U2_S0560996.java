
package ü2;
        import ij.IJ;
        import ij.ImageJ;
        import ij.ImagePlus;
        import ij.WindowManager;
        import ij.gui.ImageCanvas;
        import ij.gui.ImageWindow;
        import ij.plugin.PlugIn;
        import ij.process.ImageProcessor;

        import java.awt.Dimension;
        import java.awt.Font;
        import java.awt.GridLayout;
        import java.awt.Panel;

        import javax.swing.BorderFactory;
        import javax.swing.JPanel;
        import javax.swing.JSlider;
        import javax.swing.border.TitledBorder;
        import javax.swing.event.ChangeEvent;
        import javax.swing.event.ChangeListener;

/**
 Opens an image window and adds a panel below the image
 */
public class GLDM_U2_S0560996 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;


    public static void main(String args[]) {
        //new ImageJ();
        //IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
        IJ.open("orchid.jpg");

        GLDM_U2_S0560996 pw = new GLDM_U2_S0560996();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null)
            imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();

        origPixels = ((int []) ip.getPixels()).clone();
    }


    class CustomCanvas extends ImageCanvas {

        CustomCanvas(ImagePlus imp) {
            super(imp);
        }

    } // CustomCanvas inner class


    class CustomWindow extends ImageWindow implements ChangeListener {

        private JSlider jSliderBrightness;
        private JSlider jSlider2;
        private double brightness;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            //JPanel panel = new JPanel();
            Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 200, 100);
            jSlider2 = makeTitledSilder("Slider2-Wert", 0, 100, 50);
            panel.add(jSliderBrightness);
            panel.add(jSlider2);

            add(panel);

            pack();
        }

        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {

            JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
            Dimension preferredSize = new Dimension(width, 50);
            slider.setPreferredSize(preferredSize);
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);
            slider.setMajorTickSpacing((maxVal - minVal)/10 );
            slider.setPaintTicks(true);
            slider.addChangeListener(this);

            return slider;
        }

        private void setSliderTitle(JSlider slider, String str) {
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);
        }

        public void stateChanged( ChangeEvent e ){
            JSlider slider = (JSlider)e.getSource();

            if (slider == jSliderBrightness) {
                brightness = slider.getValue()-100;
                String str = "Helligkeit " + brightness;
                setSliderTitle(jSliderBrightness, str);
            }

            if (slider == jSlider2) {
                int value = slider.getValue();
                String str = "Slider2-Wert " + value;
                setSliderTitle(jSlider2, str);
            }

            changePixelValues(imp.getProcessor());

            imp.updateAndDraw();
        }


        private void changePixelValues(ImageProcessor ip) {

            // Array fuer den Zugriff auf die Pixelwerte
            int[] pixels = (int[])ip.getPixels();

            for (int i=0; i<height; i++) {
                for (int j=0; j<width; j++) {
                    int pos = i*width + j;
                    int argb = origPixels[pos];  // Lesen der Originalwerte

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;


                    // anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
                    // die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren

                    /*int rn, gn, bn;
                    if(r+brightness>255){
                         rn=255;
                    }else if(r+brightness<0){
                        rn=0;
                    }else{
                         rn = (int) (r + brightness);
                    }

                    if(g+brightness>255){
                         gn=255;
                    }else if(g+brightness<0){
                        gn=0;
                    }else{
                         gn = (int) (g + brightness);
                    }
                    if(b+brightness>255){
                         bn=255;
                    }else if(b+brightness<0){
                        bn=0;
                    }else{
                         bn = (int) (b + brightness);
                    }*/
                    //TODO alles wird hier verändert
                    double y=(0.299*r)+(0.587*g)+(0.114*b);
                    double u=(b-y)*0.493;
                    double v=(r-y)*0.877;
                   // System.out.println("yuv value "+y+", "+u+", "+v); //for testing

                  //    v=((v-128)*4.0)+128;
                    //y=y+102;//test brughtness on yuv
                    //TODO Helligkeit YUV
                    if(y+brightness>=255){
                        y=255;
                    }else if(y+brightness<0){
                        y=0;
                    }else{
                        y = (int) (y + brightness);
                    }                     //After adjusting the Y value the plane in YUV is shifted, so the colour in rgb will shifted too, dark color to bright and vice versa

                    //TODO Zurück nach RGB
                    int rn=(int) (y+(v/0.877));//(int) typecasting, double to int
                    int gn=(int) (((1/0.587) * y) - ((0.299/0.587)*r) - ((0.114/0.587) * b));
                    int bn=(int) (y+(u/0.493));

                    if(j==200&&i==144){
                        System.out.println("RGB"+rn+", "+gn+", "+bn);
                        System.out.println("j ist "+j+", y "+y);
                    }

                    //To set limit of 255
                    if(rn>255){
                        rn=255;
                    }else if(rn<0){
                        rn=0;
                    }
                    if(bn>255){
                        bn=255;
                    }else if(bn<0){
                        bn=0;
                    }
                    if(gn>255){
                        gn=255;
                    }else if(gn<0){
                        gn=0;
                    }





                    // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;

                }
                //System.out.println(i);
            }
        }

    } // CustomWindow inner class
}
