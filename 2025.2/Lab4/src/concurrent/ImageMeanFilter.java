import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class provides functionality to apply a mean filter to an image.
 * The mean filter is used to smooth images by averaging the pixel values
 * in a neighborhood defined by a kernel size.
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ImageMeanFilter.applyMeanFilter("input.jpg", "output.jpg", 3);
 * }
 * </pre>
 * 
 * <p>Supported image formats: JPG, PNG</p>
 * 
 * <p>Author: temmanuel@comptuacao.ufcg.edu.br</p>
 */
public class ImageMeanFilter {
    
    /**
     * Applies mean filter to an image
     * 
     * @param inputPath  Path to input image
     * @param outputPath Path to output image 
     * @param kernelSize Size of mean kernel
     * @throws IOException If there is an error reading/writing
     */
    public static void applyMeanFilter(String inputPath, String outputPath, int kernelSize, int threadsNumber) throws IOException {
        // Load image
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        
        // Create result image
        BufferedImage filteredImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        // Image processing
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        int linesPerThread = height / threadsNumber;
        java.lang.Thread[] threads = new java.lang.Thread[threadsNumber];

        for (int i = 0; i < threadsNumber; i++) {
            int startY = i * linesPerThread;
            int endY = (i == threadsNumber - 1) ? height : startY + linesPerThread;
            threads[i] = new java.lang.Thread(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        MeanFilterTask task = new MeanFilterTask(originalImage, x, y, kernelSize);
                        task.run();
                        int[] avgColor = task.getAverage();
                        int rgb = (avgColor[0] << 16) | (avgColor[1] << 8) | avgColor[2];
                        filteredImage.setRGB(x, y, rgb);
                    }
                }
            });
            threads[i].start();
        }

        for (java.lang.Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Save filtered image
        ImageIO.write(filteredImage, "jpg", new File(outputPath));
    }
    /**
     * Main method for demonstration
     * 
     * Usage: java ImageMeanFilter <input_file>
     * 
     * Arguments:
     *   input_file - Path to the input image file to be processed
     *                Supported formats: JPG, PNG
     * 
     * Example:
     *   java ImageMeanFilter input.jpg
     * 
     * The program will generate a filtered output image named "filtered_output.jpg"
     * using a 7x7 mean filter kernel
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageMeanFilter <input_file> <threads_number>");
            System.exit(1);
        }

        String inputFile = args[0];
        int threadsNumber = Integer.parseInt(args[1]);
        try {
            applyMeanFilter(inputFile, "filtered_output.jpg", 7, threadsNumber);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

}


class MeanFilterTask implements Runnable {

    private BufferedImage image;
    private int centerX;
    private int centerY;
    private int kernelSize;
    private int[] average;

    public MeanFilterTask(BufferedImage image, int centerX, int centerY, int kernelSize) {
        this.image = image;
        this.centerX = centerX;
        this.centerY = centerY;
        this.kernelSize = kernelSize;
        this.average = new int[3]; // To store average RGB values
    }

    @Override
    public void run() {
        int width = image.getWidth();
        int height = image.getHeight();
        int pad = kernelSize / 2;
        
        // Arrays for color sums
        long redSum = 0, greenSum = 0, blueSum = 0;
        int pixelCount = 0;
        
        // Process neighborhood
        for (int dy = -pad; dy <= pad; dy++) {
            for (int dx = -pad; dx <= pad; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                // Check image bounds
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    // Get pixel color
                    int rgb = image.getRGB(x, y);
                    
                    // Extract color components
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // Sum colors
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                    pixelCount++;
                }
            }
        }
        
        // Calculate average
        average = new int[]{
            (int)(redSum / pixelCount),
            (int)(greenSum / pixelCount),
            (int)(blueSum / pixelCount)
        };
    }

    public int[] getAverage() {
        return average;
    }
}