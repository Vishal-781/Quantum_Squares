package com.example.quantum_squares

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import java.lang.Math.abs

class MainActivity : AppCompatActivity() {
    private val dx = intArrayOf(1, -1, 0, 0) // Directions for x
    private val dy = intArrayOf(0, 0, -1, 1) // Directions for y
    private var turn: Long = 1
    private var currentGridSize: Int = 5 // Default grid size
    private lateinit var gameGrid: GridLayout
    private lateinit var player1Score: TextView
    private lateinit var player2Score: TextView
    private lateinit var buttons: Array<Array<Button>> // Store buttons for the grid
    var player1ScoreValue = 0
    var player2ScoreValue = 0
    var v = Array(currentGridSize) { LongArray(currentGridSize) { 0 } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the grid layout
        gameGrid = findViewById(R.id.game_grid)
        player1Score = findViewById(R.id.player1_score)
        player2Score = findViewById(R.id.player2_score)

        // Create the grid
        createGrid()

        // Set up the reset button
        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            resetGame() // Reset the game
        }
    }

    private fun createGrid() {
        currentGridSize = 5 // Set the grid size

        // Initialize the game state
        v = Array(currentGridSize) { LongArray(currentGridSize) { 0 } } // Stores particles
        buttons = Array(currentGridSize) { Array(currentGridSize) { Button(this) } } // Store buttons for the grid

        // Clear previous grid
        gameGrid.removeAllViews()
        gameGrid.rowCount = currentGridSize
        gameGrid.columnCount = currentGridSize

        // Dynamically add FrameLayouts to the grid
        for (i in 0 until currentGridSize) {
            for (j in 0 until currentGridSize) {
                val frameLayout = FrameLayout(this)
                frameLayout.layoutParams = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(i)
                    columnSpec = GridLayout.spec(j)
                    width = 185 // Set width
                    height = 185 // Set height
                    setMargins(8, 8, 8, 8) // Optional margin for separation
                }

                // Create an overlay button
                val button = Button(this)
                button.text = ""
                button.setBackgroundColor(Color.LTGRAY)
                button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                frameLayout.addView(button)

                // Add the FrameLayout to the grid
                gameGrid.addView(frameLayout)

                // Store the button in the array for later reference
                buttons[i][j] = button

                // Set the button click listener
                button.setOnClickListener {
                    onGridButtonClicked(frameLayout, i, j, v) // Handle the button click
                }
            }
        }
    }

    private fun onGridButtonClicked(frameLayout: FrameLayout, row: Int, col: Int, v: Array<LongArray>) {
        // Check if the move is valid (current player can only play in neutral or their own squares)
        if (v[row][col] * turn >= 0) {
            reaction(v, row, col, turn) // Perform the reaction

            // Determine ball color and count based on the current player's turn
            val color = if (turn == 1L) "green" else "red"
            val ballCount = abs(v[row][col]).toInt()

            // Add balls to the cell
            addMultipleBallsToCell(frameLayout, color, ballCount)

            // Switch turn
            turn *= -1
        } else {
            // Invalid move
            Toast.makeText(this, "INVALID MOVE", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMultipleBallsToCell(frameLayout: FrameLayout, color: String, ballCount: Int) {
        frameLayout.removeViewsInLayout(1, frameLayout.childCount - 1) // Clear previous balls

        val ballDrawable = when (color) {
            "green" -> R.drawable.green_particle
            "red" -> R.drawable.red_particle
            else -> 0
        }

        // Dynamically add balls based on the ballCount
        for (i in 1..ballCount) {
            val ballView = ImageView(this)
            ballView.setImageDrawable(ContextCompat.getDrawable(this, ballDrawable))

            // Set initial position and size for the ball
            ballView.layoutParams = FrameLayout.LayoutParams(100, 100).apply {
                setMargins(0, -1500, 0, 0) // Start off-screen
            }

            // Add the ball view to the FrameLayout
            frameLayout.addView(ballView)
//
//            // Animate the ball coming down into the cell
//            ballView.animate()
//                .translationY(1500f) // Move down by 1500 pixels
//                .rotation(360f) // Rotate 360 degrees
//                .setDuration(1000) // Animation duration of 1000ms
//                .start() // Start the animation
        }
    }

    private fun reaction(v: Array<LongArray>, x: Int, y: Int, turn: Long) {
        v[x][y] += turn // Add particle for the current player

        // Update button text and color for UI
        buttons[x][y].text = abs(v[x][y]).toString()
//        buttons[x][y].setBackgroundColor(if (v[x][y] > 0) Color.RED else Color.BLUE)
        buttons[x][y].background = ContextCompat.getDrawable(this, if (v[x][y] > 0) R.drawable.green_particle else R.drawable.red_particle)
        // If the number of particles in the square is less than 4, no collapse
        if (abs(v[x][y]) < 4) return

        // If collapse happens, update score and reset particles in the square
        if (turn.toInt() == 1) {
            player1ScoreValue++
            player1Score.text = "Player 1: $player1ScoreValue"
        } else {
            player2ScoreValue++
            player2Score.text = "Player 2: $player2ScoreValue"
        }

        // Check for winning conditions
        if (player1ScoreValue >= 10) {
            Toast.makeText(this, "Player 1 wins", Toast.LENGTH_SHORT).show()
            resetGame()
            return
        } else if (player2ScoreValue >= 10) {
            Toast.makeText(this, "Player 2 wins", Toast.LENGTH_SHORT).show()
            resetGame()
            return
        }

        v[x][y] = 0
        buttons[x][y].text = ""
        buttons[x][y].setBackgroundColor(Color.LTGRAY)

        // Redistribute particles to adjacent squares
        for (i in 0 until 4) {
            val nx = x + dx[i]
            val ny = y + dy[i]

            // Check if the adjacent square is within bounds
            if (nx in 0 until currentGridSize && ny in 0 until currentGridSize) {
                reaction(v, nx, ny, turn * if (v[nx][ny] * turn >= 0) 1 else -1)
            }
        }
    }

    private fun resetGame() {
        // Reset the game grid and current state
        for (i in 0 until currentGridSize) {
            for (j in 0 until currentGridSize) {
                buttons[i][j].text = "" // Reset the button text
                buttons[i][j].setBackgroundColor(Color.LTGRAY) // Reset the button color
                // Clear all balls from the FrameLayout
                val frameLayout = gameGrid.getChildAt(i * currentGridSize + j) as FrameLayout
                frameLayout.removeViewsInLayout(1, frameLayout.childCount - 1) // Clear balls
            }
        }
        v = Array(currentGridSize) { LongArray(currentGridSize) { 0 } } // Reset the game state
        player1ScoreValue = 0
        player2ScoreValue = 0
        player1Score.text = "Player 1: $player1ScoreValue"
        player2Score.text = "Player 2: $player2ScoreValue"
        turn = 1 // Reset to Player 1
    }
}
