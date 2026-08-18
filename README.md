# 2-D Feed Backward Predictive Coding

## DSAI 325 – Introduction to Information Theory
Assignment 5: Java Implementation of 2-D Feed Backward Predictive Coding

## Overview
This project implements a 2-D Feed Backward Predictive Coder in Java. The implemented solution includes:

- Three predictor types:
  - Order-1: Uses the previous pixel value (left or top) for prediction
  - Order-2: Uses the average of left and top pixels for prediction
  - Adaptive: Dynamically selects the prediction approach based on local gradient information

- Configurable quantization levels (e.g., 8, 16, 32 levels)

- Comprehensive analysis tools for evaluating compression performance

## Project Structure
- `PredictiveCoder.java`: Core implementation of the predictive coding algorithm
- `PredictiveCoderApp.java`: Application to run experiments and generate reports
- `README.md`: This file with instructions
- `input/`: Directory for input images
- `output/`: Directory for output results and reports (generated at runtime; not committed)

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 8 or above
- Input images in PNG format

### Directory Setup
1. Create an `input` directory in the same location as the Java files
2. Place your test images in the `input` directory (e.g., lena.png, cameraman.png, barbara.png)
3. The program will automatically create an `output` directory to store results

## How to Run

### Compile the Java Files
```bash
javac PredictiveCoder.java PredictiveCoderApp.java
```

### Run the Application
```bash
java PredictiveCoderApp
```

The application will:
1. Process all images in the `input` directory using different predictor types and quantization levels
2. Save reconstructed images and residual visualizations to the `output` directory
3. Generate a comprehensive report with performance metrics in `output/report.md`

## Experiment Configuration
You can modify the following parameters in the `PredictiveCoderApp.java` file:

- `TEST_IMAGES`: Array of image filenames to process
- `QUANT_LEVELS`: Array of quantization levels to test

## Expected Output
The program will generate:

1. For each combination of image, predictor, and quantization level:
   - Reconstructed image
   - Residual visualization
   - Performance metrics (MSE, compression ratio, processing times)

2. A comprehensive Markdown report with:
   - Performance metrics table
   - Comparative analysis across predictor types
   - Comparative analysis across quantization levels
   - Conclusions and observations

## Implementation Details

### PredictiveCoder Class
This class implements the core functionality of 2-D feed backward predictive coding:
- Image loading and preprocessing
- Pixel prediction using different predictor types
- Residual calculation and quantization
- Image reconstruction
- Performance metrics calculation (MSE, compression ratio)

### PredictiveCoderApp Class
This class provides a framework for running experiments:
- Test case execution across different parameters
- Results collection and analysis
- Report generation

## Theoretical Background

### Feed Backward Predictive Coding
Predictive coding works by:
1. Predicting the current pixel value based on previously encoded pixels
2. Computing the difference (residual) between the actual and predicted values
3. Quantizing and encoding the residual values
4. Using the same prediction scheme during decoding to reconstruct the image

### Predictor Types
- **Order-1**: Uses the value of a single previously encoded neighbor (typically left or top)
- **Order-2**: Uses a simple averaging of two previously encoded neighbors (typically left and top)
- **Adaptive**: Dynamically selects the prediction method based on local image characteristics

### Quantization
Quantization reduces the number of bits needed to represent residuals by mapping a range of values to a single representative value. More quantization levels result in better quality but lower compression.