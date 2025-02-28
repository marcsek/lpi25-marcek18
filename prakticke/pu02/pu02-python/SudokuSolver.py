from typing import Sequence
import os.path
import sys

sys.path[0:0] = [os.path.join(sys.path[0], "../../../examples/sat")]

import sat


Sudoku = Sequence[Sequence[int]]


class SudokuSolver:
    def q(self, r: int, c: int, v: int) -> int:
        return r * self.N**2 + c * self.N + v + 1

    def solve(self, sudoku: Sudoku) -> Sudoku:
        self.N = 9
        solver = sat.SatSolver()
        w = sat.DimacsWriter("sudoku_cnf_in.txt")

        # Pociatocne cisla
        for r in range(self.N):
            for c in range(self.N):
                if sudoku[r][c] != 0:
                    w.writeLiteral(self.q(r, c, sudoku[r][c] - 1))
                    w.finishClause()

        # Kazde cislo vyplnene
        for r in range(self.N):
            for c in range(self.N):
                for v in range(self.N):
                    w.writeLiteral(self.q(r, c, v))
                w.finishClause()

        # Neopakuju sa v riadku
        for r in range(self.N):
            for v in range(self.N):
                for c1 in range(self.N):
                    for c2 in range(c1):
                        w.writeImpl(self.q(r, c1, v), -self.q(r, c2, v))

        # Neopakuju sa v stlpci
        for c in range(self.N):
            for v in range(self.N):
                for r1 in range(self.N):
                    for r2 in range(r1):
                        w.writeImpl(self.q(r1, c, v), -self.q(r2, c, v))

        # Neopakuju sa vo stvorci
        for x in range(0, self.N, 3):
            for y in range(0, self.N, 3):
                for v in range(self.N):
                    for r1 in range(y, y + 3):
                        for c1 in range(x, x + 3):
                            for r2 in range(y, y + 3):
                                for c2 in range(x, x + 3):
                                    if self.q(r1, c1, v) != self.q(r2, c2, v):
                                        w.writeImpl(
                                            self.q(r1, c1, v), -self.q(r2, c2, v)
                                        )

        w.close()
        ok, sol = solver.solve(w, "sudoku_cnf_out.txt")

        res: Sudoku = [[0] * self.N for _ in range(self.N)]
        if ok:
            for x in sol:
                if x > 0:
                    x -= 1
                    v = x % 9 + 1
                    x //= 9
                    c = x % 9
                    x //= 9
                    r = x % 9
                    res[r][c] = v

        # all zeroes -> no solution
        return res
