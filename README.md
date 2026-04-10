# Grid Pathfinding & Algorithm Analysis (ASD 24/25)

[cite_start]This project is a Java-based application dedicated to solving complex pathfinding requirements within a discretized 2D space[cite: 16, 24]. [cite_start]It focuses on the implementation and performance analysis of a recursive Minimum Path algorithm, featuring various optimization strategies such as pruning, frontier reordering, and memoization[cite: 34, 35, 318].

## 🚀 Features

* [cite_start]**Grid Generation:** Supports deterministic patterns like Spiral, Rows, and Chessboard, as well as random distributions with configurable obstacle densities: Low (10%), Medium (25%), and High (45%)[cite: 49, 85].
* [cite_start]**Context & Complement Analysis:** Implements algorithms to determine reachable cells from an origin based on specific movement constraints—axial and diagonal—using the `camminoLibero` logic[cite: 29, 107, 122].
* [cite_start]**Optimized Pathfinding:** Features the `camminoMin` algorithm to find the shortest sequence of landmarks between an origin and a destination[cite: 32, 249].
* [cite_start]**Performance Benchmarking:** Includes a CLI-based batch mode to run massive tests and generate detailed CSV reports on execution time, memory usage (Heap), and explored nodes[cite: 655, 715, 716].
* [cite_start]**Dual Interface:** Provides a Graphical User Interface (GUI) for interactive grid creation and a Command Line Interface (CLI) for automated analysis[cite: 653, 665, 666].

## 🛠 Optimization Strategies

[cite_start]The core pathfinding algorithm can be enhanced with four toggleable optimizations[cite: 320, 699, 700]:
1.  [cite_start]**(R) Frontier Reordering:** Sorts frontier cells by their distance to the destination to find the optimal path faster[cite: 329, 814].
2.  [cite_start]**(C) Strong Condition:** Implements more aggressive pruning criteria to discard sub-optimal paths early[cite: 321, 816].
3.  [cite_start]**(M) Memoization:** Stores previously explored sequences to prevent redundant calculations, reducing execution time by nearly 100% in complex scenarios[cite: 346, 462, 819].
4.  [cite_start]**(V) Global Length Check:** Constantly compares current partial paths against the best global path found so far during recursion[cite: 337, 343].

## 📊 Performance Insights

[cite_start]Experimental results on $25\times25$ grids demonstrate that **Memoization (M)** is the most critical factor for scalability, drastically reducing the search space from millions of visited cells to just a few hundred[cite: 461, 553]. [cite_start]While the algorithm handles grids up to $100\times100$ in less than a second, the complexity becomes super-linear (estimated between $O(m \cdot n)^2$ and $O(m \cdot n)^3$) for larger dimensions[cite: 601, 605].

## 💻 Getting Started

### Prerequisites
* [cite_start]Java JDK 17 or higher[cite: 659].
* [cite_start]At least 2 GB of RAM recommended[cite: 661].

### Installation & Usage
1.  **Run the application:**
    Use the `.jar` file from the terminal[cite: 663]:
    ```bash
    java -jar ASD_24_25.jar <option>
    ```
2.  **Options:**
    * **GUI Mode:** `java -jar ASD_24_25.jar -g` [cite: 671]
    * **Batch/CLI Mode:** `java -jar ASD_24_25.jar -b <directory>` [cite: 671]

## 📂 Project Structure
* [cite_start]**AnalizCam:** Core logic for pathfinding and recursion management[cite: 249].
* [cite_start]**AgenteCamminoMinimo:** Controller for initializing searches and gathering statistics[cite: 250, 251].
* [cite_start]**Griglia/Cella:** Fundamental data structures representing the workspace and its properties[cite: 40, 41, 45].
* [cite_start]**ValutatoreGriglie:** Handles automated report generation in CSV format[cite: 715].

## 👥 Authors
* [cite_start]**Bagnalasta Federico** (Matricola n. 735695) [cite: 9]
* [cite_start]**Kovacic Matteo** (Matricola n. 736588) [cite: 9]
* [cite_start]**Zambelli Samuele** (Matricola n. 736800) [cite: 9]

---
[cite_start]*Developed for the University of Brescia - Department of Information Engineering (A.Y. 2024/2025)*[cite: 4, 10].
