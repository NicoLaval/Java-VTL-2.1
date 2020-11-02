package fr.insee.vtl.model;

import java.util.*;

/**
 * <code>Structured</code> is the base interface for representing structured data.
 */
public interface Structured {

    /**
     * Returns the structure associated to the data as a list of structure components.
     *
     * @return The structure associated to the data as a list of structure components.
     */
    DataStructure getDataStructure();

    /**
     * Returns the list of column names.
     *
     * @return The column names as a list of strings.
     */
    default List<String> getColumnNames() {
        return new ArrayList<>(getDataStructure().keySet());
    }

    /**
     * The <code>Structure</code> class represent a structure component with its name, type and role.
     */
    class Component {

        private final String name;
        private final Class<?> type;
        private final Dataset.Role role;
        private int index;

        /**
         * Constructor taking the name, type and role of the component.
         *
         * @param name A string giving the name of the structure component to create.
         * @param type A <code>Class</code> giving the type of the structure component to create.
         * @param role A <code>Role</code> giving the role of the structure component to create.
         */
        public Component(String name, Class<?> type, Dataset.Role role) {
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.role = Objects.requireNonNull(role);
        }

        public Component(Component component) {
            this.name = component.getName();
            this.type = component.getType();
            this.role = component.getRole();
        }

        int getIndex() {
            return index;
        }

        void setIndex(int index) {
            this.index = index;
        }

        /**
         * Returns the name of the component.
         *
         * @return The name of the component as a string.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the type of the component.
         *
         * @return The type of the component as an instance of <code>Class</code>.
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * Returns the role of component.
         *
         * @return The role of the component as a value of the <code>Role</code> enumeration.
         */
        public Dataset.Role getRole() {
            return role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Component component = (Component) o;
            return name.equals(component.name) &&
                    type.equals(component.type) &&
                    role == component.role;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, role);
        }

        @Override
        public String toString() {
            return "Component{" + name +
                    ", type=" + type +
                    ", role=" + role +
                    '}';
        }
    }

    class DataStructure extends LinkedHashMap<String, Dataset.Component> {

        public DataStructure(Map<String, Class<?>> types, Map<String, Dataset.Role> roles) {
            super(types.size());
            if (!types.keySet().equals(roles.keySet())) {
                throw new IllegalArgumentException("type and roles key sets inconsistent");
            }
            for (String column : types.keySet()) {
                Component component = new Component(column, types.get(column), roles.get(column));
                component.setIndex(size());
                put(column, component);
            }
        }

        public DataStructure(Collection<Component> components) {
            super(components.size());
            for (Component component : components) {
                var newComponent = new Component(component);
                newComponent.setIndex(size());
                put(newComponent.getName(), newComponent);
            }
        }

        public int indexOf(String column) {
            return get(column).getIndex();
        }

        public int indexOf(Dataset.Component component) {
            return component.getIndex();
        }

    }

    class DataPoint extends ArrayList<Object> {

        private final DataStructure dataStructure;

        private void grow(int size) {
            while (size() < size) {
                add(null);
            }
        }

        public DataPoint(DataStructure dataStructure, Map<String, Object> map) {
            super();
            grow(dataStructure.size());
            this.dataStructure = Objects.requireNonNull(dataStructure);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                this.set(entry.getKey(), entry.getValue());
            }
        }

        public DataPoint(DataStructure dataStructure, Collection<Object> collection) {
            super(dataStructure.size());
            this.dataStructure = Objects.requireNonNull(dataStructure);
            addAll(collection);
        }

        Object get(String column) {
            return get(dataStructure.indexOf(column));
        }

        Object set(String column, Object object) {
            return set(dataStructure.indexOf(column), object);
        }

    }

}
