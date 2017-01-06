# Robocode (CS255)
Artificial Intelligence coursework, designing and developing an AI for the Robocode game

## Dependencies
Other than [Robocode](http://robocode.sourceforge.net/ "Download Robocode"), which is required to run the AI simulation, the robot has no further dependencies than standard Java libraries.

## Compiling the Robot
With Robocode running, access the Source Editor using
>> CTRL+E 
From here, locate Wilde.java and use the Editor's built-in compiler to compile and package the robot with
>> CTRL+B
Save the file and, when prompted, click **YES** to save the compiled robot within Robocode's own robots directory.

## Running 'Wilde' Within Robocode
With Robocode running, begin a new battle using
>> CTRL+N
From the available robots, add *u1409675.Wilde* to the list of selected robots. Feel free to add any other robots to fight against, in melee (a multi-bot free-for-all battle) or 1-vs-1 combat.

Click **Start Battle** to then begin the simulation.

## About the AI
*Wilde* is a solution to the Robocode challenge which extends the Simple Robot class - this means it can only perform actions sequentially and is unable to target, move and fire concurrently. A detailed report is provided, covering the design and implementation of the robot's AI in detail.

As an overview, the AI is comprised of three main areas:

#### Targeting

The targeting is *Guess Factor*-based, implementing a wave-based virtual bullet system to quickly learn possible bullet hit angles based on enemy movement patterns.

It prioritises the closest known enemy as its target and is versatile across both melee & 1-vs-1 battles.

The AI is segmented to allow more accurate guesses in 1v1 combat, where scans are more frequent.

#### Movement

In melee battles, the movement operates a *Minimum Risk* algorithm, calculating which direction will be the least dangerous to move into based on the locations and states of its enemies.

In 1-vs-1 combat, a *Psuedo-Random* pattern is adopted, attempting to remain perpendicular where possible for maximum escape angle from incoming bullets while moving random distances to counter pattern recognition-based targeting.

#### Radar

The radar uses a *Weak Target Lock*, attempting to remain focused on its target but scanning the entire battlefield when said target is lost - this happens often - to either find the existing, or a better (closer) target.
