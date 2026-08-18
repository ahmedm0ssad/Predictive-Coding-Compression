import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DSAI 325 – Introduction to Information Theory
 * Assignment 5: Java Implementation of 2-D Feed Backward Predictive Coding
 * 
 * This application runs the 2-D Feed Backward Predictive Coder on
 * test images with different predictor types and quantization levels.
 * It collects results and generates reports for analysis.
 */
public class PredictiveCoderApp {
    
    // Configuration for experiments
    private static final String[] TEST_IMAGES = {"Ahmed.jpg", "Maha.jpg", "Moaa.jpg"};
    private static final int[] QUANT_LEVELS = {8, 16, 32};
    private static final DecimalFormat df = new DecimalFormat("#.###");
    
    public static void main(String[] args) {
        String inputDir = "input";
        String outputBaseDir = "output";
        
        // Create directories if they don't exist
        try {
            Files.createDirectories(Paths.get(inputDir));
            Files.createDirectories(Paths.get(outputBaseDir));
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
            return;
        }
        
        System.out.println("Starting 2-D Feed Backward Predictive Coding experiments...");
        System.out.println("Input directory: " + inputDir);
        System.out.println("Output directory: " + outputBaseDir);
        
        // Prepare result collection for report
        List<ExperimentResult> results = new ArrayList<>();
        
        // Run experiments for all combinations of images, predictors, and quantization levels
        for (String imageName : TEST_IMAGES) {
            String imagePath = inputDir + File.separator + imageName;
            
            // Check if file exists
            if (!new File(imagePath).exists()) {
                System.out.println("WARNING: Image " + imagePath + " not found. Skipping...");
                continue;
            }
            
            for (PredictiveCoder.PredictorType predictor : PredictiveCoder.PredictorType.values()) {
                for (int quantLevels : QUANT_LEVELS) {
                    try {
                        // Create experiment identifier
                        String experimentId = imageName.replace(".", "_") + "_" + 
                                              predictor.name() + "_" + 
                                              "Q" + quantLevels;
                        
                        String outputDir = outputBaseDir + File.separator + experimentId;
                        
                        System.out.println("Running experiment: " + experimentId);
                        
                        // Run the experiment
                        PredictiveCoder coder = new PredictiveCoder(imagePath, predictor, quantLevels);
                        long startTime = System.currentTimeMillis();
                        coder.encode();
                        long encodingTime = System.currentTimeMillis() - startTime;
                        
                        // Verify by decoding
                        startTime = System.currentTimeMillis();
                        // Decoding the encoded data to verify correctness
                        coder.decode();
                        long decodingTime = System.currentTimeMillis() - startTime;
                        
                        // Calculate metrics
                        double mse = coder.calculateMSE();
                        double compressionRatio = coder.calculateCompressionRatio();
                        
                        // Save results
                        coder.saveResults(outputDir);
                        
                        // Record the experiment result
                        ExperimentResult result = new ExperimentResult(
                            imageName,
                            predictor,
                            quantLevels,
                            mse,
                            compressionRatio,
                            encodingTime,
                            decodingTime
                        );
                        results.add(result);
                        
                        // Print the result
                        System.out.println("  MSE: " + df.format(mse));
                        System.out.println("  Compression Ratio: " + df.format(compressionRatio) + ":1");
                        System.out.println("  Encoding Time: " + encodingTime + " ms");
                        System.out.println("  Decoding Time: " + decodingTime + " ms");
                        System.out.println();
                        
                    } catch (IOException e) {
                        System.err.println("Error processing " + imageName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        // Generate the report
        try {
            generateReport(results, outputBaseDir + File.separator + "report.md");
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
        
        System.out.println("All experiments completed. Results saved to " + outputBaseDir);
    }
    
    /**
     * Generates a Markdown report with the experiment results
     */
    private static void generateReport(List<ExperimentResult> results, String reportPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportPath))) {
            writer.println("# 2-D Feed Backward Predictive Coding Report");
            writer.println("\nDSAI 325 – Introduction to Information Theory");
            writer.println("Assignment 5");
            writer.println("\nDate: April 28, 2025");
            writer.println("\n## Experiment Results");
            
            // Table header
            writer.println("\n### Performance Metrics");
            writer.println("\n| Image | Predictor | Quant. Levels | MSE | Compression Ratio | Encoding Time (ms) | Decoding Time (ms) |");
            writer.println("|-------|-----------|---------------|-----|-------------------|-------------------|-------------------|");
            
            // Table content
            for (ExperimentResult result : results) {
                writer.println("| " + result.imageName + 
                             " | " + result.predictor + 
                             " | " + result.quantLevels + 
                             " | " + df.format(result.mse) + 
                             " | " + df.format(result.compressionRatio) + ":1" +
                             " | " + result.encodingTime +
                             " | " + result.decodingTime + " |");
            }
            
            // Compare predictor types
            writer.println("\n## Comparative Analysis");
            writer.println("\n### Predictor Types Comparison");
            writer.println("\nThe following analysis compares the different predictor types across all test images and quantization levels:");
            
            // Group results by predictor type
            for (PredictiveCoder.PredictorType predictor : PredictiveCoder.PredictorType.values()) {
                writer.println("\n#### " + predictor + " Predictor");
                writer.println("\nAverage Performance:");
                
                double avgMse = results.stream()
                    .filter(r -> r.predictor == predictor)
                    .mapToDouble(r -> r.mse)
                    .average()
                    .orElse(0);
                
                double avgCompression = results.stream()
                    .filter(r -> r.predictor == predictor)
                    .mapToDouble(r -> r.compressionRatio)
                    .average()
                    .orElse(0);
                
                writer.println("- Average MSE: " + df.format(avgMse));
                writer.println("- Average Compression Ratio: " + df.format(avgCompression) + ":1");
                writer.println("\nStrengths and Weaknesses:");
                
                switch (predictor) {
                    case ORDER_1:
                        writer.println("- Simple implementation with low computational complexity");
                        writer.println("- Works well for images with horizontal gradual changes");
                        writer.println("- Less effective for images with complex textures or diagonal patterns");
                        break;
                    case ORDER_2:
                        writer.println("- Better prediction in areas with both horizontal and vertical features");
                        writer.println("- Captures more spatial correlation than Order-1");
                        writer.println("- May struggle with complex textures or sharp edges");
                        break;
                    case ADAPTIVE:
                        writer.println("- Adapts to local image characteristics");
                        writer.println("- Performs better around edges and in textured regions");
                        writer.println("- Higher computational complexity than simpler predictors");
                        break;
                }
            }
            
            // Compare quantization levels
            writer.println("\n### Quantization Levels Comparison");
            writer.println("\nThe following analysis compares different quantization levels across all test images and predictor types:");
            
            for (int quantLevels : QUANT_LEVELS) {
                writer.println("\n#### " + quantLevels + " Quantization Levels");
                
                double avgMse = results.stream()
                    .filter(r -> r.quantLevels == quantLevels)
                    .mapToDouble(r -> r.mse)
                    .average()
                    .orElse(0);
                
                double avgCompression = results.stream()
                    .filter(r -> r.quantLevels == quantLevels)
                    .mapToDouble(r -> r.compressionRatio)
                    .average()
                    .orElse(0);
                
                writer.println("- Average MSE: " + df.format(avgMse));
                writer.println("- Average Compression Ratio: " + df.format(avgCompression) + ":1");
                writer.println("- Trade-off: " + getQuantizationTradeoffDescription(quantLevels));
            }
            
            // Conclusion
            writer.println("\n## Conclusion");
            writer.println("\nBased on the experimental results, we can draw the following conclusions:");
            writer.println("\n1. The adaptive predictor generally achieves the best reconstruction quality (lowest MSE) but at the cost of higher computational complexity.");
            writer.println("2. Higher quantization levels provide better image quality but lower compression ratios.");
            writer.println("3. The effectiveness of each predictor depends on the image characteristics:");
            writer.println("   - Order-1 works well for images with gradual horizontal changes");
            writer.println("   - Order-2 provides better results for images with both horizontal and vertical features");
            writer.println("   - Adaptive performs best for images with complex textures and edges");
            writer.println("\n4. The optimal choice of predictor and quantization level depends on the specific application requirements regarding image quality versus compression ratio.");
            
            // Code overview section
            writer.println("\n## Code Implementation");
            writer.println("\nThe implementation consists of the following key components:");
            writer.println("\n### PredictiveCoder Class");
            writer.println("- Handles the core functionality of 2-D feed backward predictive coding");
            writer.println("- Implements three predictor types: Order-1, Order-2, and Adaptive");
            writer.println("- Supports configurable quantization levels");
            writer.println("- Provides methods for encoding, decoding, and calculating performance metrics");
            
            writer.println("\n### PredictiveCoderApp Class");
            writer.println("- Provides a framework for running experiments with different parameters");
            writer.println("- Collects and analyzes results");
            writer.println("- Generates this report");
            
            // Full code listing referral
            writer.println("\n### Full Code Listing");
            writer.println("\nFor the complete code implementation, please refer to the following files:");
            writer.println("- `PredictiveCoder.java`: Core implementation of the predictive coding algorithm");
            writer.println("- `PredictiveCoderApp.java`: Application to run experiments and generate reports");
            
        }
    }
    
    /**
     * Returns a description of the trade-off for a given quantization level
     */
    private static String getQuantizationTradeoffDescription(int quantLevels) {
        if (quantLevels <= 8) {
            return "Higher compression ratio but visible quality degradation";
        } else if (quantLevels <= 16) {
            return "Good balance between compression and quality";
        } else {
            return "Better quality preservation but lower compression ratio";
        }
    }
    
    /**
     * Simple class to store experiment results
     */
    private static class ExperimentResult {
        String imageName;
        PredictiveCoder.PredictorType predictor;
        int quantLevels;
        double mse;
        double compressionRatio;
        long encodingTime;
        long decodingTime;
        
        public ExperimentResult(String imageName, PredictiveCoder.PredictorType predictor, 
                               int quantLevels, double mse, double compressionRatio,
                               long encodingTime, long decodingTime) {
            this.imageName = imageName;
            this.predictor = predictor;
            this.quantLevels = quantLevels;
            this.mse = mse;
            this.compressionRatio = compressionRatio;
            this.encodingTime = encodingTime;
            this.decodingTime = decodingTime;
        }
    }
}