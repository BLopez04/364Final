import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
/**
 * A simple Swing GUI for loading a TSPLIB .tsp file,
 * displaying the cities, and computing a tour using the nearest neighbor heuristic.
 *
 * @author javiergs
 * @version 1.0
 */
public class TspFrame extends JFrame {

  private final MapPanel mapPanel = new MapPanel();
  private final JTextArea log = new JTextArea(8, 60);
  private List<City> cities = List.of();
  private List<Integer> tour = List.of();

  public TspFrame() {
    super("Demo (TSPLIB + Nearest Neighbor)");
    log.setEditable(false);
    log.setBackground(new Color(200, 255, 220));
    JButton loadBtn = new JButton("Load .tsp");
    JButton solveBtn = new JButton("Nearest Neighbor");
    JButton clearBtn = new JButton("Clear Tour");
    loadBtn.addActionListener(e -> onLoad());
    solveBtn.addActionListener(e -> onSolve());
    clearBtn.addActionListener(e -> onClear());
    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(loadBtn);
    top.add(solveBtn);
    top.add(clearBtn);
    setLayout(new BorderLayout());
    add(top, BorderLayout.NORTH);
    add(mapPanel, BorderLayout.CENTER);
    add(new JScrollPane(log), BorderLayout.SOUTH);
    log.append("Ready: Load a Waterloo TSPLIB file and draw cities.\n");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(900, 650);
    setLocationRelativeTo(null);
  }

  // get and load file to solver
  private void onLoad() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a TSPLIB .tsp file");
    chooser.setFileFilter(new FileNameExtensionFilter("TSPLIB (*.tsp)", "tsp"));
    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
    File f = chooser.getSelectedFile();
    try {
      cities = TspParser.load(f);
      tour = List.of();
      mapPanel.setCities(cities);
      TspBlackboard.getInstance().setCities(cities);
      log.append("\nLoaded: " + f.getAbsolutePath() + "\n");
      log.append("Cities: " + cities.size() + "\n");
    } catch (Exception ex) {
      log.append("\nERROR: " + ex.getMessage() + "\n");
    }
  }

  // our new solver using threads
  private void onSolve() {
    if (cities == null || cities.size() < 2) {
      log.append("\nLoad a file first.\n");
      return;
    }

    log.append("\nStarting distributed TSP solve...\n");

    String broker = "tcp://broker.hivemq.com:1883";

    try {
      // Start up our solver threads, reserve a processor for the manager
      int numThreads = Runtime.getRuntime().availableProcessors() - 1;
      if (numThreads < 1) numThreads = 1;

      log.append("Starting " + numThreads + " solver threads\n");

      for (int i = 0; i < numThreads; i++) {
        NearestNeighborSolver solver = new NearestNeighborSolver(broker);
        new Thread(solver).start();
      }

      // Start the manager and its subscribers
      TspManager manager = new TspManager(broker);

      // Wait for all results
      log.append("Waiting for all solver results...\n");
      manager.waitForAll();

      // Get the best tour from the blackboard
      List<Integer> bestTour = TspBlackboard.getInstance().getBestTour();
      double bestCost = TspBlackboard.getInstance().getBestCost();

      // Update UI
      mapPanel.setTour(bestTour);
      log.append("Best tour length: " + String.format("%.3f", bestCost) + "\n");
      log.append("Distributed TSP solve complete.\n");

    } catch (Exception e) {
      log.append("\nERROR: " + e.getMessage() + "\n");
      e.printStackTrace();
    }
  }


  // terminate solver and all sub threads
  // note: could be done using a pipeline
  private void onClear() {
    tour = List.of();
    mapPanel.setTour(tour);

    TspBlackboard.getInstance().setCities(City.STOP);
    log.append("\nTour cleared.\n");
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new TspFrame().setVisible(true));
  }

}
