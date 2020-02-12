import java.util.*;

/**
 * Класс для решения головоломки, имплементация интерфейса ConundrumSolver
 * Решение основано на имплементации алгоритма для поиска оптимального пути обхода графа <a href="https://ru.wikipedia.org/wiki/A*">A*</a>
 */
public class ConundrumSolverImpl implements ConundrumSolver {

    /**
     * Последовательность ходов, приводящих головоломку в эталонное состояние
     */
    private List<Integer> result = new ArrayList<>();

    /**
     * Метод для решения головоломки.
     *
     * @param initialState исходное состояние
     * @return решение головоломки
     */
    @Override
    public int[] resolve(int[] initialState) {
        if (!checkRightArrayFormat(initialState)) {
            return new int[0];
        }
        Graph initialGraph = new Graph(initialState);

        /*
         * Для нахождения оптимального пути используем PriorityQueue.
         * Упорядочивание происходит в зависимости от величины f(x) для переданных состояний
         */
        PriorityQueue<State> priorityQueue = new PriorityQueue<>(10, new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return Integer.compare(f(o1), f(o2));
            }
        });

        // Добавляем в очередь начальное состояние
        priorityQueue.add(new State(null, initialGraph));

        // Бесконечный цикл для поиска оптимального пути
        while (true) {
            State state = priorityQueue.poll();
            // Если решение найдено сохраняем последовательность
            if (Objects.requireNonNull(state).getGraph().isSolve()) {
                saveToResult(state);
                break;
            }
            for (Graph graph : state.graph.neighbors()) {
                /*
                 * Если текущего состояния нет в цепочке предыдущих состояний,
                 * добавляем это состояние в очередь
                 */
                if (!containsInPath(state, graph))
                    priorityQueue.add(new State(state, graph));
            }
        }
        return listToArray(result);
    }

    /**
     * Метод для вычисления функции f(x).
     * f(x) вычисляется на основе двух компонентов:
     * 1) h(x) - эвристическая составляющая. Количество элементов в текущем состоянии,
     * которые расположены не на своих местах;
     * 2) g(x) - стоимость достижения текущего узла из начального, того в котором изначально
     * распологался 0
     *
     * @param state {@code State}, для которого расчитывается функция
     * @return результат функции f(x)
     */
    private static int f(State state) {
        State state2 = state;
        int g = 0;   // g(x)
        int h = state.getGraph().getH();  // h(x)
        while (true) {
            g++;
            state2 = state2.previousState;
            if (state2 == null) {
                return h + g; // f(x)
            }
        }
    }

    /**
     * Сохранение результата головоломки
     *
     * @param state {@code State} решенной головоломки, которое также содержит ссылки на предыдущие
     *              состояния {@code Graph}, из которых извлекаем результирующую
     *              последовательность ходов и помещаем в {@code List} result
     */
    private void saveToResult(State state) {
        State state2 = state;
        while (true) {
            int previousZeroPosition = state2.graph.getZeroPosition();
            state2 = state2.previousState;
            if (state2 == null) {
                Collections.reverse(result);
                return;
            }
            int[] graphState = state2.graph.getCurrentState();
            result.add(graphState[previousZeroPosition]);
        }
    }

    /**
     * Метод для проверки, есть ли текущее состояние {@code Graph} в цепочке предыдущих {@code State}
     * для исключения повторного размещения в очередь
     *
     * @param state ссылка на цепочку предыдущих сосстояний
     * @param graph текущее состояние {@code Graph}
     * @return текущее состояние находится в цепочке предыдущих
     */
    private boolean containsInPath(State state, Graph graph) {
        State state2 = state;
        while (true) {
            if (state2.graph.equals(graph)) return true;
            state2 = state2.previousState;
            if (state2 == null) return false;
        }
    }

    // Метод для возвращения массива элементов, хранящихся в списке
    private int[] listToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Проверка входящего массива на соответствие заданному формату и отсутствие лишних элементов
     *
     * @param initialState входящий массив
     * @return входящий массив соответствует требованиям
     */
    private boolean checkRightArrayFormat(int[] initialState) {
        if (initialState == null) {
            System.out.println("Введен пустой массив");
            return false;
        }
        if (initialState.length == Graph.STANDARD.length) {
            for (int i : Graph.STANDARD) {
                int count = 0;
                for (int j : initialState) {
                    if (i == j) {
                        count++;
                    }
                }
                if (count != 1) {
                    System.out.println("Введена неправильная последовательность элементов");
                    return false;
                }
            }
            return true;
        }
        System.out.println("Введена неправильная последовательность элементов");
        return false;
    }

    /**
     * Класс описывающий состояние головоломки
     */
    private static class Graph {

        /**
         * Эталонное расположение узлов в графе
         */
        static final int[] STANDARD = new int[]{1, 2, 3, 4, 0, 5, 6, 7};
        /**
         * Текущее расположение узлов
         */
        private int[] currentState;
        /**
         * Список всех узлов
         */
        private List<Node> nodes;
        /**
         * Позицию нулевого узла в масссиве currentState
         */
        private int zeroPosition;
        /**
         * h(x)
         */
        private int h;

        /**
         * Констркутор графа на основе переданного расположения узлов
         *
         * @param initialState расположение узлов
         */
        Graph(int[] initialState) {
            this.currentState = new int[initialState.length];
            System.arraycopy(initialState, 0, currentState, 0, currentState.length);
            h = 0;
            /*
             * В этом цикле рассчитываем эвристическую составляющую f(x),
             * количество элементов расположенных не на своих местах, за исключением нулевого узла,
             * а так же находим позицию нулевого узла
             */
            for (int i = 0; i < currentState.length; i++) {
                if (currentState[i] != STANDARD[i] && currentState[i] != 0) {
                    h += 1;
                }
                if (currentState[i] == 0) {
                    zeroPosition = i;
                }
            }
            this.nodes = createNodes(currentState);
        }

        int[] getCurrentState() {
            return currentState;
        }

        int getH() {
            return h;
        }

        int getZeroPosition() {
            return zeroPosition;
        }

        /**
         * Проверка сборки головоломки, приведения ее в эталонное состояние
         *
         * @return h(x) == 0
         */
        boolean isSolve() {
            return h == 0;
        }

        /**
         * Метод для создания списка узлов графа с учетом текущего расположения
         *
         * @param array текущее расположение
         * @return {@code List} узлов графа
         */
        private List<Node> createNodes(int[] array) {
            final int[] array2 = new int[array.length];
            System.arraycopy(array, 0, array2, 0, array.length);
            return new ArrayList<Node>() {{
                add(new Node(array2[0], new int[]{1, 2}));
                add(new Node(array2[1], new int[]{0, 2, 3}));
                add(new Node(array2[2], new int[]{0, 1, 5}));
                add(new Node(array2[3], new int[]{1, 4, 6}));
                add(new Node(array2[4], new int[]{3, 5}));
                add(new Node(array2[5], new int[]{2, 4, 7}));
                add(new Node(array2[6], new int[]{3, 7}));
                add(new Node(array2[7], new int[]{5, 6}));
            }};
        }

        /**
         * Метод для созданния {@code Graph<{@code Graph}>},
         * получаемых при перемещении нулевого {@code Node} ко всем соседним
         *
         * @return {@code Set<{@code Graph}>} всех смежных состояний
         */
        Set<Graph> neighbors() {
            Set<Graph> graphStates = new HashSet<>();
            for (int i : nodes.get(zeroPosition).getConnections()) {
                graphStates.add(changeState(nodes.get(i), nodes.get(zeroPosition)));
            }
            return graphStates;
        }

        /**
         * Метод для перемещения двух смежных {@code Node} один из которых нулевой
         *
         * @param node     узел, который мы заменяем на нулевой
         * @param zeroNode нулевой узел
         * @return измененный {@code Graph}
         */
        private Graph changeState(Node node, Node zeroNode) {
            int[] changeState = new int[this.currentState.length];
            System.arraycopy(this.currentState, 0, changeState, 0, currentState.length);
            int previousPosition = 0;
            for (int i = 0; i < changeState.length; i++) {
                if (changeState[i] == zeroNode.getValue()) {
                    changeState[i] = node.getValue();
                    previousPosition = i;
                    break;
                }
            }
            for (int i = 0; i < changeState.length; i++) {
                if (i == previousPosition) {
                    continue;
                }
                if (changeState[i] == node.getValue()) {
                    changeState[i] = zeroNode.getValue();
                    break;
                }
            }
            return new Graph(changeState);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Graph graph = (Graph) o;
            return Arrays.equals(currentState, graph.currentState);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(currentState);
        }
    }

    /**
     * Класс отражающий текущее состояние графа и цепочку предыдущих
     */
    private static class State {
        /**
         * Ссылка на предыдущее состояние.
         * Может быть null в начальном состоянии
         */
        private State previousState;
        /**
         * Текущее состояние
         */
        private ConundrumSolverImpl.Graph graph;

        /**
         * Конуструктор текущего состояния графа
         *
         * @param previousState ссылка на предыдущее состояние. Может быть null
         * @param graph         текущее состояние
         */
        private State(State previousState, ConundrumSolverImpl.Graph graph) {
            this.previousState = previousState;
            this.graph = graph;
        }

        ConundrumSolverImpl.Graph getGraph() {
            return graph;
        }
    }

    /**
     * Класс описывающий узлы графа
     */
    private static class Node {
        /**
         * Числовое значение в узле
         */
        int value;
        /**
         * Позиции узлов, соединенных с данным
         */
        int[] connections;

        /**
         * @param value       числовое значение в узле
         * @param connections узлы расположенные на противоположных концах ребер.
         *                    Индексация в соответствии со схемой графа
         */
        Node(int value, int[] connections) {
            this.value = value;
            this.connections = connections;
        }

        int getValue() {
            return value;
        }

        int[] getConnections() {
            return connections;
        }
    }
}
