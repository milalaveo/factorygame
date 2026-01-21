package ui;

import domain.contract.GameEventListener;
import domain.model.GameEvent;
import service.factory.FactoryStats;
import service.factory.GameController;
import shared.Config;
import shared.FactoryConfig;
import shared.TimeFormatter;
import shared.Validation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Swing UI to play the factory game //TODO: refactor to MVC
public final class FactoryGameUI extends JFrame {
    private static final int LOG_ROWS = 12;
    private static final int LOG_COLUMNS = 60;
    private static final int STATS_REFRESH_MS = 500;

    private final GameController controller;
    private final JTextArea logArea;
    private final JLabel producedLabel;
    private final JLabel processedLabel;
    private final JLabel bufferLabel;
    private final JLabel goalLabel;
    private final JLabel timeLabel;
    private final JLabel resourceLabel;
    private final JLabel allocationSummaryLabel;
    private final JButton startButton;
    private final JButton stopButton;
    private final JSpinner supplierPointsSpinner;
    private final JSpinner machinePointsSpinner;
    private final JSpinner bufferPointsSpinner;
    private final JSpinner timeLimitSpinner;
    private final Timer statsTimer;
    private final Timer gameTimer;
    private int goalTarget;
    private boolean goalReached;
    private int remainingSeconds;

    public FactoryGameUI() {
        super("Factory Game - Producer Consumer Demo");
        this.controller = new GameController();
        this.logArea = new JTextArea(LOG_ROWS, LOG_COLUMNS);
        this.producedLabel = new JLabel("Produced: 0");
        this.processedLabel = new JLabel("Processed: 0");
        this.bufferLabel = new JLabel("Buffer: 0/0");
        this.goalLabel = new JLabel("Goal: 0/0");
        this.timeLabel = new JLabel("Time: 0s");
        this.resourceLabel = new JLabel("Resources: " + Config.RESOURCE_POINTS);
        this.allocationSummaryLabel = new JLabel("Suppliers: 0  Machines: 0  Buffer: 0");
        this.startButton = new JButton("Start");
        this.stopButton = new JButton("Stop");
        this.supplierPointsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Config.RESOURCE_POINTS, 1));
        this.machinePointsSpinner = new JSpinner(new SpinnerNumberModel(4, 0, Config.RESOURCE_POINTS, 1));
        this.bufferPointsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Config.RESOURCE_POINTS, 1));
        this.timeLimitSpinner = new JSpinner(new SpinnerNumberModel(60, 10, 300, 5));

        this.statsTimer = new Timer(STATS_REFRESH_MS, event -> refreshStats());
        this.statsTimer.start();
        this.gameTimer = new Timer(1000, event -> tickGameTimer());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(720, 520));
        buildUI();
        wireEvents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.stop();
            }
        });
    }

    private void buildUI() {
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controls = new JPanel(new GridLayout(4, 4, 10, 5));
        controls.setBorder(BorderFactory.createTitledBorder("Allocate Resources"));
        controls.add(new JLabel("Supplier Points"));
        controls.add(new JLabel("Machine Points"));
        controls.add(new JLabel("Buffer Points"));
        controls.add(new JLabel("Remaining"));
        controls.add(supplierPointsSpinner);
        controls.add(machinePointsSpinner);
        controls.add(bufferPointsSpinner);
        controls.add(resourceLabel);
        controls.add(new JLabel("Resulting Counts"));
        controls.add(new JLabel(""));
        controls.add(new JLabel(""));
        controls.add(allocationSummaryLabel);
        controls.add(new JLabel("Time Limit (s)"));
        controls.add(timeLimitSpinner);
        controls.add(new JLabel(""));
        controls.add(new JLabel(""));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 5));
        buttons.add(startButton);
        buttons.add(stopButton);
        stopButton.setEnabled(false);

        JPanel stats = new JPanel(new GridLayout(1, 5, 10, 5));
        stats.setBorder(BorderFactory.createTitledBorder("Live Stats"));
        stats.add(producedLabel);
        stats.add(processedLabel);
        stats.add(bufferLabel);
        stats.add(goalLabel);
        stats.add(timeLabel);

        JPanel north = new JPanel(new BorderLayout(10, 10));
        north.add(controls, BorderLayout.CENTER);
        north.add(buttons, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(stats, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        GameEventListener listener = this::appendEvent;
        controller.addListener(listener);

        startButton.addActionListener(event -> {
            FactoryConfig config = readConfig();
            goalTarget = Config.GOAL_PROCESSED;
            goalReached = false;
            goalLabel.setText("Goal: 0/" + goalTarget);
            remainingSeconds = (Integer) timeLimitSpinner.getValue();
            timeLabel.setText("Time: " + remainingSeconds + "s");
            controller.start(config);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            gameTimer.start();
        });

        stopButton.addActionListener(event -> {
            controller.stop();
            gameTimer.stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });

        supplierPointsSpinner.addChangeListener(event -> updateAllocationSummary());
        machinePointsSpinner.addChangeListener(event -> updateAllocationSummary());
        bufferPointsSpinner.addChangeListener(event -> updateAllocationSummary());
        updateAllocationSummary();
    }

    private FactoryConfig readConfig() {
        int supplierPoints = (Integer) supplierPointsSpinner.getValue();
        int machinePoints = (Integer) machinePointsSpinner.getValue();
        int bufferPoints = (Integer) bufferPointsSpinner.getValue();
        int producers = Config.BASE_PRODUCER_COUNT + supplierPoints;
        int consumers = Config.BASE_CONSUMER_COUNT + machinePoints;
        int capacity = Config.BASE_BUFFER_CAPACITY + bufferPoints;
        Validation.requirePositive(producers, "producers");
        Validation.requirePositive(consumers, "consumers");
        Validation.requirePositive(capacity, "capacity");
        return new FactoryConfig(
                producers,
                consumers,
                capacity,
                Config.PRODUCER_MIN_DELAY_MS,
                Config.PRODUCER_MAX_DELAY_MS
        );
    }

    private void appendEvent(GameEvent event) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format(
                    "%s [%s] %s (buffer %d)%n",
                    TimeFormatter.format(event.getTimestamp()),
                    event.getActor(),
                    event.getAction(),
                    event.getBufferSize()
            );
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void refreshStats() {
        if (!controller.isRunning()) {
            return;
        }
        FactoryStats stats = controller.getStats();
        producedLabel.setText("Produced: " + stats.getProducedCount());
        processedLabel.setText("Processed: " + stats.getProcessedCount());
        bufferLabel.setText("Buffer: " + stats.getBufferSize() + "/" + stats.getBufferCapacity());
        goalLabel.setText("Goal: " + stats.getProcessedCount() + "/" + goalTarget);
        if (!goalReached && stats.getProcessedCount() >= goalTarget) {
            goalReached = true;
            handleGoalReached();
        }
    }

    private void handleGoalReached() {
        appendMessage("Goal reached! Factory target met.");
        controller.stop();
        gameTimer.stop();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void handleGoalFailed() {
        appendMessage("Time's up. Goal not met.");
        controller.stop();
        gameTimer.stop();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void tickGameTimer() {
        if (!controller.isRunning()) {
            return;
        }
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        timeLabel.setText("Time: " + remainingSeconds + "s");
        if (remainingSeconds == 0 && !goalReached) {
            gameTimer.stop();
            handleGoalFailed();
        }
    }

    private void updateAllocationSummary() {
        int supplierPoints = (Integer) supplierPointsSpinner.getValue();
        int machinePoints = (Integer) machinePointsSpinner.getValue();
        int bufferPoints = (Integer) bufferPointsSpinner.getValue();
        int spent = supplierPoints + machinePoints + bufferPoints;
        int remaining = Config.RESOURCE_POINTS - spent;
        resourceLabel.setText("Resources: " + remaining);
        int producers = Config.BASE_PRODUCER_COUNT + supplierPoints;
        int consumers = Config.BASE_CONSUMER_COUNT + machinePoints;
        int capacity = Config.BASE_BUFFER_CAPACITY + bufferPoints;
        allocationSummaryLabel.setText("Suppliers: " + producers + "  Machines: " + consumers + "  Buffer: " + capacity);
        startButton.setEnabled(remaining == 0 && !controller.isRunning());
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format("%s [Goal] %s%n",
                    TimeFormatter.format(java.time.Instant.now()),
                    message);
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
