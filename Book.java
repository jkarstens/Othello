/*
 * An Othello book that keeps information regarding good opening moves.
 */
 public final class Book {
   static Othello.Board[] book = new Othello.Board[76];

   book[0] = new Othello.Board();

   private Book() {

   }
 }
