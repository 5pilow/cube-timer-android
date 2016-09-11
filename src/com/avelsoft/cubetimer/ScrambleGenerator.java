package com.avelsoft.cubetimer;

import java.util.ArrayList;
import java.util.List;

public class ScrambleGenerator {
	
	
	public static String generate(int size) {
		
		int length = 18;
		List<Move> moves = new ArrayList<Move>();
		
		int lastAxis = -1;
		int lastMove = -1;
		int lastLastAxis = -1;
		int lastLastMove = -1;
		
		for (int i = 0; i < length; i++) {
			
			// Mouvement principal
			int axis, move;
			boolean correct;
			
			do {
				correct = true;
				
				axis = (int) Math.floor(Math.random() * 3);
				move = (int) Math.floor(Math.random() * 2);
				
				if (axis == lastAxis && lastMove == move) correct = false;
				
				if (moves.size() >= 2) {
					if (axis == lastAxis && axis == lastLastAxis && move == lastLastMove) correct = false;
				}
				
			} while (!correct);
			
			if (moves.size() >= 1) {
				lastLastAxis = lastAxis;
				lastLastMove = lastMove;
			}
			lastAxis = axis;
			lastMove = move;
			
			// Dérivations (secondaire)
			int option = (int) Math.floor(Math.random() * 3);
			
			moves.add(new Move(axis, move, option));
		}
		
		
		String scramble = "";
		String[] MOVES = new String[] {"UD", "LR", "FB"};
		String OPTIONS = " '2";
		
		for (int i = 0; i < moves.size(); i++) {
			
			Move move = moves.get(i);
			
			scramble += MOVES[move.axis].charAt(move.move);
			
			if (move.option > 0) {
				scramble += OPTIONS.charAt(move.option);
			}
			scramble += " ";
		}
		
		return scramble;
	}
	
	private static class Move {
		
		int axis; // UD, RL, FB
		int move; // quel coté ?
		int option; // " ", "'", "2"
		
		public Move(int axis, int move, int option) {
			this.axis = axis;
			this.move = move;
			this.option = option;
		}
	}
}
