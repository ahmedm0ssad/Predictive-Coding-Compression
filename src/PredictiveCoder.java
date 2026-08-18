import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * DSAI 325 – Introduction to Information Theory
 * Assignment 5: Java Implementation of 2-D Feed Backward Predictive Coding
 * 
 * This class implements a 2-D Feed Backward Predictive Coder with three predictor types:
 * - Order-1 (Previous pixel)
 * - Order-2 (Average of left and top pixels)
 * - Adaptive (Weighted average based on gradient)
 * 
 * It supports configurable quantization levels for residual encoding.
 */
public class PredictiveCoder {
    
    // Predictor types
    public enum PredictorType {
        ORDER_1,    // Previous pixel prediction
        ORDER_2,    // Average of left and top pixels
        ADAPTIVE    // Adaptive prediction based on gradient
    }
    
    private int[][] originalImage;
    private int[][] reconstructedImage;
    private int[][] quantizedResiduals;
    private int width;
    private int height;
    private int maxPixelValue = 255;
    private int minPixelValue = 0;
    private PredictorType predictorType;
    private int quantizationLevels;
    
    /**
     * Constructor for the predictive coder
     * 
     * @param imagePath Path to the grayscale image file
     * @param predictor The type of predictor to use
     * @param quantLevels Number of quantization levels for residuals
     * @throws IOException If there's an error reading the image
     */
    public PredictiveCoder(String imagePath, PredictorType predictor, int quantLevels) throws IOException {
        this.predictorType = predictor;
        this.quantizationLevels = quantLevels;
        
        // Read and preprocess the image
        loadImage(imagePath);
    }
    
    /**
     * Loads an image from file and converts it to grayscale
     * 
     * @param imagePath Path to the image file
     * @throws IOException If there's an error reading the image
     */
    private void loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
        
        originalImage = new int[height][width];
        
        // Convert image to grayscale and store in 2D array
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Convert to grayscale using standard luminance formula
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                originalImage[y][x] = gray;
            }
        }
    }
    
    /**
     * Encodes the image using the specified predictor and quantization levels
     */
    public void encode() {
        reconstructedImage = new int[height][width];
        quantizedResiduals = new int[height][width];
        
        // Define quantization step size
        double stepSize = (double)(maxPixelValue - minPixelValue) / quantizationLevels;
        
        // Process the image pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Predict the current pixel value
                int predictedValue = predictPixel(x, y, reconstructedImage);
                
                // Calculate the residual
                int residual = originalImage[y][x] - predictedValue;
                
                // Quantize the residual
                int quantizedResidual = (int)Math.round(residual / stepSize);
                quantizedResiduals[y][x] = quantizedResidual;
                
                // Reconstruct the pixel value for use in further predictions
                int dequantizedResidual = (int)(quantizedResidual * stepSize);
                int reconstructedValue = predictedValue + dequantizedResidual;
                
                // Clamp the value to valid pixel range
                reconstructedValue = Math.min(Math.max(reconstructedValue, minPixelValue), maxPixelValue);
                reconstructedImage[y][x] = reconstructedValue;
            }
        }
    }
    
    /**
     * Decodes the image from quantized residuals
     * 
     * @return The reconstructed image as a 2D array
     */
    public int[][] decode() {
        int[][] decodedImage = new int[height][width];
        double stepSize = (double)(maxPixelValue - minPixelValue) / quantizationLevels;
        
        // Process the image pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Predict the current pixel value
                int predictedValue = predictPixel(x, y, decodedImage);
                
                // Dequantize the residual
                int dequantizedResidual = (int)(quantizedResiduals[y][x] * stepSize);
                
                // Reconstruct the pixel value
                int reconstructedValue = predictedValue + dequantizedResidual;
                
                // Clamp the value to valid pixel range
                reconstructedValue = Math.min(Math.max(reconstructedValue, minPixelValue), maxPixelValue);
                decodedImage[y][x] = reconstructedValue;
            }
        }
        
        return decodedImage;
    }
    
    /**
     * Predicts the pixel value based on the selected predictor type
     * 
     * @param x X-coordinate of the pixel
     * @param y Y-coordinate of the pixel
     * @param image The image to use for prediction
     * @return The predicted pixel value
     */
    private int predictPixel(int x, int y, int[][] image) {
        // For the first pixel, we can't predict using neighbors
        if (x == 0 && y == 0) {
            return 128; // Default to middle gray
        }
        
        switch (predictorType) {
            case ORDER_1:
                return predictOrder1(x, y, image);
            case ORDER_2:
                return predictOrder2(x, y, image);
            case ADAPTIVE:
                return predictAdaptive(x, y, image);
            default:
                return 0;
        }
    }
    
    /**
     * Order-1 predictor: Uses the previous pixel
     * If at the start of a row, uses the pixel above
     */
    private int predictOrder1(int x, int y, int[][] image) {
        if (x > 0) {
            return image[y][x - 1]; // Use left pixel
        } else if (y > 0) {
            return image[y - 1][x]; // Use top pixel at start of row
        } else {
            return 128; // Default for the first pixel
        }
    }
    
    /**
     * Order-2 predictor: Average of left and top pixels
     */
    private int predictOrder2(int x, int y, int[][] image) {
        int left = (x > 0) ? image[y][x - 1] : 0;
        int top = (y > 0) ? image[y - 1][x] : 0;
        
        if (x > 0 && y > 0) {
            return (left + top) / 2;
        } else if (x > 0) {
            return left;
        } else if (y > 0) {
            return top;
        } else {
            return 128; // Default for the first pixel
        }
    }
    
    /**
     * Adaptive predictor: Uses gradient information to choose weights
     */
    private int predictAdaptive(int x, int y, int[][] image) {
        if (x == 0 && y == 0) {
            return 128; // Default for the first pixel
        }
        
        // If we're at an edge, use simpler prediction
        if (x == 0) {
            return image[y - 1][x]; // Top pixel
        } 
        if (y == 0) {
            return image[y][x - 1]; // Left pixel
        }
        
        // Get neighboring pixels
        int left = image[y][x - 1];
        int top = image[y - 1][x];
        int topLeft = image[y - 1][x - 1];
        
        // Compute horizontal and vertical gradients
        int horizontalGradient = Math.abs(left - topLeft);
        int verticalGradient = Math.abs(top - topLeft);
        
        // Adaptive prediction based on gradients
        if (horizontalGradient < verticalGradient) {
            // Horizontal edge detected, use left pixel
            return left;
        } else if (verticalGradient < horizontalGradient) {
            // Vertical edge detected, use top pixel
            return top;
        } else {
            // No clear edge, use average
            return (left + top) / 2;
        }
    }
    
    /**
     * Calculates Mean Squared Error between original and reconstructed images
     * 
     * @return The MSE value
     */
    public double calculateMSE() {
        double sumSquaredError = 0.0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int error = originalImage[y][x] - reconstructedImage[y][x];
                sumSquaredError += error * error;
            }
        }
        
        return sumSquaredError / (width * height);
    }
    
    /**
     * Calculates the compression ratio achieved
     * 
     * @return The compression ratio (original size / encoded size)
     */
    public double calculateCompressionRatio() {
        // Original size: 8 bits per pixel
        int originalBits = width * height * 8;
        
        // Encoded size: log2(quantizationLevels) bits per residual
        int bitsPerResidual = (int)Math.ceil(Math.log(quantizationLevels) / Math.log(2));
        int encodedBits = width * height * bitsPerResidual;
        
        return (double)originalBits / encodedBits;
    }
    
    /**
     * Saves the original, reconstructed images and the residuals
     * 
     * @param outputDir Directory to save the output images
     * @throws IOException If there's an error saving the images
     */
    public void saveResults(String outputDir) throws IOException {
        // Create output directory if it doesn't exist
        Files.createDirectories(Paths.get(outputDir));
        
        // Save the reconstructed image
        BufferedImage reconstructed = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = reconstructedImage[y][x];
                int rgb = (gray << 16) | (gray << 8) | gray;
                reconstructed.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(reconstructed, "png", new File(outputDir + "/reconstructed.png"));
        
        // Save the residuals visualization (shifted to display centered at gray)
        BufferedImage residuals = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Scale residuals to be visible (centered around 128)
                int resValue = 128 + (quantizedResiduals[y][x] * 16);
                resValue = Math.min(Math.max(resValue, 0), 255);
                
                int rgb = (resValue << 16) | (resValue << 8) | resValue;
                residuals.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(residuals, "png", new File(outputDir + "/residuals.png"));
    }
    
    /**
     * Returns the original image as a 2D array
     */
    public int[][] getOriginalImage() {
        return originalImage;
    }
    
    /**
     * Returns the reconstructed image as a 2D array
     */
    public int[][] getReconstructedImage() {
        return reconstructedImage;
    }
    
    /**
     * Returns the quantized residuals as a 2D array
     */
    public int[][] getQuantizedResiduals() {
        return quantizedResiduals;
    }
}