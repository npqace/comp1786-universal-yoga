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

interface DropdownProps {
  data: { label: string; value: any }[];
  onSelect: (item: { label: string; value: any }) => void;
  defaultButtonText: string;
  value?: any; // Add value prop
}

const CustomDropdown: FC<DropdownProps> = ({ data, onSelect, defaultButtonText, value }) => {
  const DropdownButton = useRef<View>(null);
  const [visible, setVisible] = useState(false);
  const [selected, setSelected] = useState<{ label: string; value: any } | undefined>();
  const [dropdownTop, setDropdownTop] = useState(0);
  const [dropdownLeft, setDropdownLeft] = useState(0);
  const [dropdownWidth, setDropdownWidth] = useState(0);

  // This effect syncs the internal state with the parent's state
  useEffect(() => {
    const currentSelection = data.find(d => d.value === value);
    setSelected(currentSelection);
  }, [value, data]);

  const toggleDropdown = (): void => {
    if (visible) {
      setVisible(false);
    } else {
      openDropdown();
    }
  };

  const openDropdown = (): void => {
    DropdownButton.current?.measure((_fx, _fy, _w, h, _px, py) => {
      setDropdownTop(py);
      setDropdownLeft(_px);
      setDropdownWidth(_w);
    });
    setVisible(true);
  };

  const onItemPress = (item: { label: string; value: any }): void => {
    // The parent's onSelect will trigger a state change, which will re-render this component
    onSelect(item); 
    setVisible(false);
  };

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
      <Text style={styles.buttonText} numberOfLines={1}>
        {(selected && selected.label) || defaultButtonText}
      </Text>
      <Ionicons name="chevron-down" size={18} color={colors.textLight} />
      <Modal visible={visible} transparent animationType="none">
        <TouchableWithoutFeedback onPress={() => setVisible(false)}>
          <View style={styles.modalOverlay}>
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
