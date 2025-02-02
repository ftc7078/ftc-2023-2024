/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.old_teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;

@Disabled
@Autonomous(name = "PoPAuto One Cone", group = "Autonomous")

public class PoPAutoOneCone extends LinearOpMode implements MecanumDrive.TickCallback {


    private MecanumDrive mecanumDrive = new MecanumDrive();


    private PoPRobot robot = new PoPRobot();
    private int sleeveCode;

    //This is Object Detection (OD)
    //private UGObjectDetector OD = new UGObjectDetector();
    //private int DWAS = 2;//Duck Wheel Acceleration Speed
    private enum ScoringDirection {SCORE_LEFT, SCORE_RIGHT}

    ;
    private ScoringDirection scoringDirection = ScoringDirection.SCORE_LEFT;
    private int path = 0;


    @Override
    public void runOpMode() {

        robot.init(hardwareMap, telemetry, this);
        mecanumDrive.init(hardwareMap, telemetry, this);
        mecanumDrive.setCountPerDegree(7.5);

        robot.setMotorDirections(mecanumDrive);
        mecanumDrive.setupTickCallback(this);
        //mecanumDrive.setMotorDirections(FORWARD, REVERSE, FORWARD, REVERSE);
        //This is Object Detection (OD)
        //OD.init(hardwareMap, telemetry,this);
        //mecanumDrive.setupTickCallback(robot);
        //robot.multishotDelay = 225;


        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");
        long start = System.currentTimeMillis();
        boolean timesUp=false;
        while (!isStarted() ||  (sleeveCode<1 && !timesUp)) {
            sleep(50);
            sleeveCode = robot.getSleevePosition();
            if (sleeveCode > 3) {
                scoringDirection = ScoringDirection.SCORE_RIGHT;
                path = sleeveCode - 3;
            } else {
                scoringDirection = ScoringDirection.SCORE_LEFT;
                path = sleeveCode;
            }
            telemetry.addData("Sleeve", sleeveCode);
            telemetry.addData("path", path);

            telemetry.addData("Scoring Direction", scoringDirection);
            telemetry.update();
            timesUp = (System.currentTimeMillis() - start) > 4000;
        }
        waitForStart();
        robot.stopVision();
        robot.clawGrab();
        telemetry.addData("Sleeve", sleeveCode);
        telemetry.addData("Path", path);
        telemetry.update();
        System.out.printf("Sleeve %d   Path %d\n", sleeveCode, path);

        if (scoringDirection == ScoringDirection.SCORE_LEFT) {
            mecanumDrive.leftStrafe(4,1);
            robot.turnTurretTo(32, 1);
        } else {
            mecanumDrive.rightStrafe(4,1);
            robot.turnTurret(-35, 1);
        }
        //robot.setElevatorPosition(3200);
        robot.turnArmTo(120);
        robot.setWrist(0.6, 0);

        mecanumDrive.forward(16, 0.5);
        waitForElevatorAndTurret();
        robot.turnArmTo(150);
        sleep(500);
        robot.clawRelease();
        sleep(500);
        robot.turnArmTo(0);
        robot.turnTurretTo(0, 1);
       //robot.setElevatorPosition(0);
        mecanumDrive.forward(10, 0.5);
        waitForElevatorAndTurret();

        if (path == 1) {
            mecanumDrive.leftTurn(90, 0.5);
            mecanumDrive.forward(20, 0.5);
        } else if (path == 3) {
            mecanumDrive.rightTurn(90, 0.5);
            mecanumDrive.forward(24, 0.5);
        }

        while (opModeIsActive()) {
            double speed = 1;

            speed = (gamepad1.right_trigger * 0.5) + 0.5;
            double fwd = addDeadZone(gamepad1.left_stick_y);
            double strafe = addDeadZone(gamepad1.left_stick_x);
            double rot = addDeadZone(gamepad1.right_stick_x);

            fwd = fwd * speed;
            strafe = strafe * speed * 1.6;
            if (strafe > 1) {
                strafe = 1;
            } else if (strafe < -1) {
                strafe = -1;
            }
            rot = rot * speed;
            mecanumDrive.setMotors(strafe, fwd, rot, 1);
        }

    }

    public void waitForElevatorAndTurret() {
        while (robot.turretTickResult() || robot.elevatorTickResult()) {
            if (!opModeIsActive()) {
                return;
            }
            sleep(50);
        }
    }
    public void tickCallback() {
        if (gamepad1.b) {
            mecanumDrive.debugMode = true;
        } else if (gamepad1.x) {
            mecanumDrive.debugMode = false;
        }
        telemetry.addData("debugMode", mecanumDrive.debugMode);
        telemetry.update();
        robot.turretTickResult();
        robot.elevatorTickResult();
    }

    double addDeadZone(double input) {
        if (Math.abs(input) < 0.1) {
            return (0.0);
        }
        return (input);
    }

}
