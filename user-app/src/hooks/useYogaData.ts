import { useState, useEffect, useCallback } from 'react';
import { YogaService } from '../services/yogaService';
import { YogaClass, LoadingState, SearchFilters } from '../types';
import { YogaCourse } from '../types/YogaCourse';

export function useYogaClasses() {
  const [classes, setClasses] = useState<YogaClass[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  const fetchClasses = useCallback(async () => {
    try {
      console.log('📱 Hook: Starting to fetch classes...');
      setLoading({ isLoading: true });
      const fetchedClasses = await yogaService.getClassesWithCourses();
      console.log('📱 Hook: Fetched classes count:', fetchedClasses.length);
      console.log('📱 Hook: First class sample:', fetchedClasses[0]);
      setClasses(fetchedClasses);
      setLoading({ isLoading: false });
      console.log('📱 Hook: State updated successfully');
    } catch (error) {
      console.error('📱 Hook: Error fetching classes:', error);
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch classes' 
      });
    }
  }, [yogaService]);

  useEffect(() => {
    console.log('📱 Hook: useEffect triggered, calling fetchClasses');
    fetchClasses();
  }, [fetchClasses]);

  const refresh = useCallback(() => {
    fetchClasses();
  }, [fetchClasses]);

  return { classes, loading, refresh };
}

export function useYogaCourses() {
  const [courses, setCourses] = useState<YogaCourse[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  const fetchCourses = useCallback(async () => {
    try {
      setLoading({ isLoading: true });
      const fetchedCourses = await yogaService.getAllCourses();
      setCourses(fetchedCourses);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch courses' 
      });
    }
  }, [yogaService]);

  useEffect(() => {
    fetchCourses();
  }, [fetchCourses]);

  const refresh = useCallback(() => {
    fetchCourses();
  }, [fetchCourses]);

  return { courses, loading, refresh };
}

export function useClassSearch() {
  const [searchResults, setSearchResults] = useState<YogaClass[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: false });
  const yogaService = YogaService.getInstance();

  const searchClasses = useCallback(async (filters: SearchFilters) => {
    try {
      setLoading({ isLoading: true });
      const results = await yogaService.searchClasses(filters);
      setSearchResults(results);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to search classes' 
      });
    }
  }, [yogaService]);

  const clearSearch = useCallback(() => {
    setSearchResults([]);
    setLoading({ isLoading: false });
  }, []);

  return { searchResults, loading, searchClasses, clearSearch };
}

export function useClassDetail(classId: string | undefined) {
  const [classDetail, setClassDetail] = useState<YogaClass | null>(null);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  const fetchClassDetail = useCallback(async () => {
    if (!classId) {
      setLoading({ isLoading: false, error: 'Class ID is required' });
      return;
    }

    try {
      setLoading({ isLoading: true });
      const fetchedClass = await yogaService.getClassById(classId);
      setClassDetail(fetchedClass);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch class details' 
      });
    }
  }, [classId, yogaService]);

  useEffect(() => {
    fetchClassDetail();
  }, [fetchClassDetail]);

  const refresh = useCallback(() => {
    fetchClassDetail();
  }, [fetchClassDetail]);

  return { classDetail, loading, refresh };
} 