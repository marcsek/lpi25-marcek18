#!/usr/bin/env python3
import os
import sys

sys.path[0:0] = [os.path.join(sys.path[0], "..", "..", "..", "examples/sat")]
import sat

Vlavo = "vlavo"
Vpravo = "vpravo"
Breh = [Vlavo, Vpravo]
Str2Breh = {y: x for x, y in enumerate(Breh)}

Vlk = "vlk"
Koza = "koza"
Kapusta = "kapusta"
Gazda = "gazda"
Kto = [Vlk, Koza, Kapusta, Gazda]
Str2Kto = {y: x for x, y in enumerate(Kto)}


class VlkKozaKapusta(object):
    def vlavo(self, X, K):
        return 1 + 4 * K + Str2Kto[X]

    def prevez(self, S, X, K):
        return 1 + 4 * (self.N + 1) + 4 * 4 * K + 4 * Str2Kto[X] + Str2Breh[S]

    def vyries(self, pociatocnyStav, N):
        self.N = N
        w = sat.DimacsWriter("vlk_koza_kapusta_in.txt")

        # Počiatočný a koncový stav

        for X, strana in pociatocnyStav.items():
            sign = 1 if strana == Vlavo else -1
            w.writeClause([sign * self.vlavo(X, 0)])

        for X in Kto:
            w.writeClause([-self.vlavo(X, self.N)])

        # Vykonanie práve jednej akcie

        # - aspoň jedna akcia v K-tom kroku:

        for K in range(self.N):
            for S in Breh:
                for X in Kto:
                    w.writeLiteral(self.prevez(S, X, K))
            w.finishClause()

        # - najviac jedna akcia v K-tom kroku:

        for K in range(self.N):
            for S1 in Breh:
                for S2 in Breh:
                    for X1 in Kto:
                        for X2 in Kto:
                            if S1 != S2 or X1 != X2:
                                l1 = -self.prevez(S1, X1, K)
                                l2 = -self.prevez(S2, X2, K)
                                w.writeClause([l1, l2])

        # Podmienky a efekty akcií

        # - podmienky akcií

        for K in range(self.N):
            for X in Kto:
                w.writeImpl(self.prevez(Vpravo, X, K), self.vlavo(X, K))
                w.writeImpl(self.prevez(Vpravo, X, K), self.vlavo(Gazda, K))
                w.writeImpl(self.prevez(Vlavo, X, K), -self.vlavo(X, K))
                w.writeImpl(self.prevez(Vlavo, X, K), -self.vlavo(Gazda, K))

        # - efekty akcií

        for K in range(self.N):
            for X in Kto:
                w.writeImpl(self.prevez(Vpravo, X, K), -self.vlavo(X, K + 1))
                w.writeImpl(self.prevez(Vpravo, X, K), -self.vlavo(Gazda, K + 1))
                w.writeImpl(self.prevez(Vlavo, X, K), self.vlavo(X, K + 1))
                w.writeImpl(self.prevez(Vlavo, X, K), self.vlavo(Gazda, K + 1))

        # Zotrvačnosť stavových atómov

        for K in range(self.N):
            for X in Kto:

                w.writeLiteral(-self.vlavo(X, K))
                w.writeLiteral(self.vlavo(X, K + 1))
                w.writeLiteral(self.prevez(Vpravo, X, K))
                if X == Gazda:
                    for X2 in Kto:
                        if X2 != Gazda:
                            w.writeLiteral(self.prevez(Vpravo, X2, K))
                w.finishClause()

                w.writeLiteral(self.vlavo(X, K))
                w.writeLiteral(-self.vlavo(X, K + 1))
                w.writeLiteral(self.prevez(Vlavo, X, K))
                if X == Gazda:
                    for X2 in Kto:
                        if X2 != Gazda:
                            w.writeLiteral(self.prevez(Vlavo, X2, K))
                w.finishClause()

        # Obmedzenia úlohy

        for K in range(self.N + 1):
            w.writeLiteral(-self.vlavo(Vlk, K))
            w.writeLiteral(-self.vlavo(Koza, K))
            w.writeLiteral(self.vlavo(Gazda, K))
            w.finishClause()

            w.writeLiteral(self.vlavo(Vlk, K))
            w.writeLiteral(self.vlavo(Koza, K))
            w.writeLiteral(-self.vlavo(Gazda, K))
            w.finishClause()

            w.writeLiteral(-self.vlavo(Kapusta, K))
            w.writeLiteral(-self.vlavo(Koza, K))
            w.writeLiteral(self.vlavo(Gazda, K))
            w.finishClause()

            w.writeLiteral(self.vlavo(Kapusta, K))
            w.writeLiteral(self.vlavo(Koza, K))
            w.writeLiteral(-self.vlavo(Gazda, K))
            w.finishClause()

        solver = sat.SatSolver()
        ok, sol = solver.solve(w, "vlk_koza_kapusta_out.txt")

        akcie = list()
        for s in sol:
            if s > 0 and s > 4 * (self.N + 1):
                noOffset = s - 4 * (self.N + 1) - 1
                noOffset %= 16
                akcie.append(Kto[noOffset // 4])

        return akcie


if __name__ == "__main__":
    s = {Vlk: Vpravo, Koza: Vlavo, Kapusta: Vpravo, Gazda: Vlavo}
    print(VlkKozaKapusta().vyries(s, 1))
