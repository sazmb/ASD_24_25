# Grid Pathfinding & Algorithm Analysis (ASD 24/25)

[cite_start]This project is a Java-based application dedicated to solving complex pathfinding requirements within a discretized 2D space. It focuses on the implementation and performance analysis of a recursive Minimum Path algorithm, featuring various optimization strategies such as pruning, frontier reordering, and memoization.

## 🚀 Features

* **Grid Generation:** Supports deterministic patterns like Spiral, Rows, and Chessboard, as well as random distributions with configurable obstacle densities: Low (10%), Medium (25%), and High (45%)[cite: 49, 85].
* **Context & Complement Analysis:** Implements algorithms to determine reachable cells from an origin based on specific movement constraints—axial and diagonal—using the `camminoLibero` logic.
* **Optimized Pathfinding:** Features the `camminoMin` algorithm to find the shortest sequence of landmarks between an origin and a destination.
* **Performance Benchmarking:** Includes a CLI-based batch mode to run massive tests and generate detailed CSV reports on execution time, memory usage (Heap), and explored nodes.
* **Dual Interface:** Provides a Graphical User Interface (GUI) for interactive grid creation and a Command Line Interface (CLI) for automated analysis.

## 🛠 Optimization Strategies

The core pathfinding algorithm can be enhanced with four toggleable optimizations:
1.  **(R) Frontier Reordering:** Sorts frontier cells by their distance to the destination to find the optimal path faster.
2.  **(C) Strong Condition:** Implements more aggressive pruning criteria to discard sub-optimal paths early.
3.  **(M) Memoization:** Stores previously explored sequences to prevent redundant calculations, reducing execution time by nearly 100% in complex scenarios.
4.  **(V) Global Length Check:** Constantly compares current partial paths against the best global path found so far during recursion.

## 📊 Performance Insights

[cite_start]Experimental results on $25\times25$ grids demonstrate that **Memoization (M)** is the most critical factor for scalability, drastically reducing the search space from millions of visited cells to just a few hundred. While the algorithm handles grids up to $100\times100$ in less than a second, the complexity becomes super-linear (estimated between $O(m \cdot n)^2$ and $O(m \cdot n)^3$) for larger dimensions.

## 💻 Getting Started

### Prerequisites
* Java JDK 17 or higher.
* At least 2 GB of RAM recommended.

### Installation & Usage
1.  **Run the application:**
    Use the `.jar` file from the terminal
    ```bash
    java -jar ASD_24_25.jar <option>
    ```
2.  **Options:**
    * **GUI Mode:** `java -jar ASD_24_25.jar -g` 
    * **Batch/CLI Mode:** `java -jar ASD_24_25.jar -b <directory>` 

## 📂 Project Structure
* **AnalizCam:** Core logic for pathfinding and recursion management
* **AgenteCamminoMinimo:** Controller for initializing searches and gathering statistics.
* **Griglia/Cella:** Fundamental data structures representing the workspace and its properties.
* **ValutatoreGriglie:** Handles automated report generation in CSV format

## 👥 Authors
* **Bagnalasta Federico** (Matricola n. 735695)
* [cite_start]**Kovacic Matteo** (Matricola n. 736588) 
* **Zambelli Samuele** (Matricola n. 736800)

---
*Developed for the University of Brescia - Department of Information Engineering (A.Y. 2024/2025)*
