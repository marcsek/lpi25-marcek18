#!/usr/bin/env python3

import os
import sys

sys.path[0:0] = [
    os.path.join(sys.path[0], os.path.join("..", "..", "..", "examples", "sat"))
]

import sat


class HamiltonianCycle(object):
    def v(self, pos, i):
        return pos * self.N + i + 1

    def find(self, edges):
        """Finds a hamiltonian cycle in the oriented graph given by 'edges' or
        returns an empty list if there is none.

        @param edges an incidence matrix, edges[i][j] is True if there
        is an edge from i to j
        """

        solver = sat.SatSolver()
        w = sat.DimacsWriter("hamiltonian_cycle_cnf_in.txt")
        self.N = len(edges)

        # pre každé pos aspoň jedno z v(pos,i) je pravdivé
        for pos in range(self.N):
            for i in range(self.N):
                w.writeLiteral(self.v(pos, i))
            w.finishClause()

        # pre každé pos nie sú dve rôzne v(pos,i) a v(pos,j) pravdivé naraz
        for pos in range(self.N):
            for i in range(self.N):
                for notI in range(self.N):
                    if i != notI:
                        w.writeImpl(self.v(pos, i), -self.v(pos, notI))

        # pre každé i nie sú dve rôzne v(pos1,i) a v(pos2,i) pravdivé naraz
        for i in range(self.N):
            for pos in range(self.N):
                for notPos in range(self.N):
                    if pos != notPos:
                        w.writeImpl(self.v(pos, i), -self.v(notPos, i))

        # ak nie je v grafe hrana z i do j, tak pre každé pos nesmú
        # platiť v(pos,i) a v(pos+1,j)
        for i in range(self.N):
            for j in range(self.N):
                for pos in range(self.N):
                    pos1, pos2 = pos, (pos + 1) % self.N
                    if not edges[i][j]:
                        w.writeImpl(self.v(pos1, i), -self.v(pos2, j))

        w.close()
        ok, sol = solver.solve(w, "hamiltonian_cycle_cnf_out.txt")

        if not ok:
            return []

        result = []
        for i, s in enumerate(sol):
            if s < 0:
                continue

            row = i // self.N
            result.append(i - row * self.N)

        return result
