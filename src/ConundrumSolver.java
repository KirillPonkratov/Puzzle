/**
 * Интерфейс решения головоломки.
 */
public interface ConundrumSolver {
    /**
     * Поиск минимального количества шагов для решения головоломки
     *
     * @param initialState исходное состояние
     * @return решение
     */
    int[] resolve(int[] initialState);
}

