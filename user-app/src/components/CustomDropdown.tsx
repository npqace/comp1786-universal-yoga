/**
 * @file CustomDropdown.tsx
 * @description A reusable dropdown component that displays a list of items in a modal.
 */
import React, { useState, useRef, FC, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  FlatList,
  StyleSheet,
  TouchableWithoutFeedback,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, typography, borderRadius } from '../styles/globalStyles';

/**
 * @interface DropdownProps
 * @description Props for the CustomDropdown component.
 * @property {{ label: string; value: any }[]} data - The array of items to display in the dropdown.
 * @property {(item: { label: string; value: any }) => void} onSelect - Function to call when an item is selected.
 * @property {string} defaultButtonText - The text to display on the button when no item is selected.
 * @property {any} [value] - The currently selected value, used to sync with parent state.
 */
interface DropdownProps {
  data: { label: string; value: any }[];
  onSelect: (item: { label: string; value: any }) => void;
  defaultButtonText: string;
  value?: any; // Add value prop
}

/**
 * @component CustomDropdown
 * @description A reusable dropdown component that displays a list of items in a modal.
 * @param {DropdownProps} props - The props for the component.
 */
const CustomDropdown: FC<DropdownProps> = ({ data, onSelect, defaultButtonText, value }) => {
  const DropdownButton = useRef<View>(null);
  const [visible, setVisible] = useState(false);
  const [selected, setSelected] = useState<{ label: string; value: any } | undefined>();
  const [dropdownTop, setDropdownTop] = useState(0);
  const [dropdownLeft, setDropdownLeft] = useState(0);
  const [dropdownWidth, setDropdownWidth] = useState(0);

  /**
   * @effect
   * @description This effect syncs the internal selected state of the dropdown with the `value` prop passed from the parent component.
   * This ensures that if the parent's state changes, the dropdown's display text updates accordingly.
   */
  useEffect(() => {
    const currentSelection = data.find(d => d.value === value);
    setSelected(currentSelection);
  }, [value, data]);

  /**
   * @method toggleDropdown
   * @description Toggles the visibility of the dropdown modal. If it's visible, it closes it; otherwise, it opens it.
   */
  const toggleDropdown = (): void => {
    if (visible) {
      setVisible(false);
    } else {
      openDropdown();
    }
  };

  /**
   * @method openDropdown
   * @description Measures the position and size of the dropdown button to position the modal correctly, then makes the modal visible.
   */
  const openDropdown = (): void => {
    DropdownButton.current?.measure((_fx, _fy, _w, h, _px, py) => {
      setDropdownTop(py);
      setDropdownLeft(_px);
      setDropdownWidth(_w);
    });
    setVisible(true);
  };

  /**
   * @method onItemPress
   * @description Handles the press event on a dropdown item. It calls the parent's onSelect callback and closes the dropdown.
   * @param {{ label: string; value: any }} item - The selected item.
   */
  const onItemPress = (item: { label: string; value: any }): void => {
    // The parent's onSelect will trigger a state change, which will re-render this component
    onSelect(item); 
    setVisible(false);
  };

  /**
   * @method renderItem
   * @description Renders a single item in the dropdown list.
   * @param {{ item: { label: string; value: any } }} props - The item to render.
   * @returns {React.ReactElement} A touchable row for the dropdown list.
   */
  const renderItem = ({ item }: { item: { label: string; value: any } }) => (
    <TouchableOpacity style={styles.item} onPress={() => onItemPress(item)}>
      <Text style={styles.itemText}>{item.label}</Text>
    </TouchableOpacity>
  );

  return (
    <TouchableOpacity
      ref={DropdownButton}
      style={styles.button}
      onPress={toggleDropdown}
    >
      {/* Display the selected label or the default text */}
      <Text style={styles.buttonText} numberOfLines={1}>
        {(selected && selected.label) || defaultButtonText}
      </Text>
      <Ionicons name="chevron-down" size={18} color={colors.textLight} />
      {/* Modal for the dropdown list */}
      <Modal visible={visible} transparent animationType="none">
        <TouchableWithoutFeedback onPress={() => setVisible(false)}>
          <View style={styles.modalOverlay}>
            {/* The dropdown list is positioned absolutely based on the button's position */}
            <View
              style={[
                styles.dropdown,
                { top: dropdownTop, left: dropdownLeft, width: dropdownWidth },
              ]}
            >
              <FlatList
                data={data}
                renderItem={renderItem}
                keyExtractor={(item, index) => index.toString()}
              />
            </View>
          </View>
        </TouchableWithoutFeedback>
      </Modal>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.background,
    height: 50,
    width: '100%',
    paddingHorizontal: spacing.md,
    borderRadius: borderRadius.medium,
    borderWidth: 1,
    borderColor: colors.border,
    justifyContent: 'space-between',
  },
  buttonText: {
    flex: 1,
    fontSize: 16,
    color: colors.text,
  },
  modalOverlay: {
    width: '100%',
    height: '100%',
  },
  dropdown: {
    position: 'absolute',
    backgroundColor: colors.surface,
    borderRadius: borderRadius.medium,
    borderWidth: 1,
    borderColor: colors.border,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 5,
    maxHeight: 200, // Ensure the dropdown is scrollable if it has many items
  },
  item: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  itemText: {
    fontSize: 16,
    color: colors.text,
  },
});

export default CustomDropdown;