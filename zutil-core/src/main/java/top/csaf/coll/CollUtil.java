package top.csaf.coll;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import top.csaf.lang.JsonReflectUtil;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * 集合工具类
 */
@Slf4j
public class CollUtil {

  // region org.apache.commons.collections4.CollectionUtils

  public static java.lang.Object get(java.lang.Object object, int index) {
    return org.apache.commons.collections4.CollectionUtils.get(object, index);
  }

  public static <K, V> java.util.Map.Entry<K, V> get(java.util.Map<K, V> map, int index) {
    return org.apache.commons.collections4.CollectionUtils.get(map, index);
  }

  /**
   * 是否 集合为 null 或 没有元素
   *
   * @param coll 集合
   * @return 是否 集合为 null 或 没有元素
   */
  public static boolean isEmpty(java.util.Collection<?> coll) {
    return org.apache.commons.collections4.CollectionUtils.isEmpty(coll);
  }

  public static int size(java.lang.Object object) {
    return org.apache.commons.collections4.CollectionUtils.size(object);
  }

  public static <C> boolean addAll(java.util.Collection<C> collection, java.lang.Iterable<? extends C> iterable) {
    return org.apache.commons.collections4.CollectionUtils.addAll(collection, iterable);
  }

  public static <C> boolean addAll(java.util.Collection<C> collection, java.util.Iterator<? extends C> iterator) {
    return org.apache.commons.collections4.CollectionUtils.addAll(collection, iterator);
  }

  public static <C> boolean addAll(java.util.Collection<C> collection, java.util.Enumeration<? extends C> enumeration) {
    return org.apache.commons.collections4.CollectionUtils.addAll(collection, enumeration);
  }

  public static <I, O> java.util.Collection<O> collect(java.util.Iterator<I> inputIterator, org.apache.commons.collections4.Transformer<? super I, ? extends O> transformer) {
    return org.apache.commons.collections4.CollectionUtils.collect(inputIterator, transformer);
  }

  public static <I, O> java.util.Collection<O> collect(java.lang.Iterable<I> inputCollection, org.apache.commons.collections4.Transformer<? super I, ? extends O> transformer) {
    return org.apache.commons.collections4.CollectionUtils.collect(inputCollection, transformer);
  }

  public static <I, O, R extends Collection<? super O>> R collect(final Iterator<? extends I> inputIterator, final Transformer<? super I, ? extends O> transformer, final R outputCollection) {
    return org.apache.commons.collections4.CollectionUtils.collect(inputIterator, transformer, outputCollection);
  }

  public static <I, O, R extends Collection<? super O>> R collect(final Iterable<? extends I> inputCollection, final Transformer<? super I, ? extends O> transformer, final R outputCollection) {
    return org.apache.commons.collections4.CollectionUtils.collect(inputCollection, transformer, outputCollection);
  }

  public static boolean containsAll(java.util.Collection<?> coll1, java.util.Collection<?> coll2) {
    return org.apache.commons.collections4.CollectionUtils.containsAll(coll1, coll2);
  }

  public static <E> java.util.Collection<E> removeAll(java.util.Collection<E> collection, java.util.Collection<?> remove) {
    return org.apache.commons.collections4.CollectionUtils.removeAll(collection, remove);
  }

  public static <E> java.util.Collection<E> removeAll(java.lang.Iterable<E> collection, java.lang.Iterable<? extends E> remove, org.apache.commons.collections4.Equator<? super E> equator) {
    return org.apache.commons.collections4.CollectionUtils.removeAll(collection, remove, equator);
  }

  public static <E> java.util.Collection<E> retainAll(java.lang.Iterable<E> collection, java.lang.Iterable<? extends E> retain, org.apache.commons.collections4.Equator<? super E> equator) {
    return org.apache.commons.collections4.CollectionUtils.retainAll(collection, retain, equator);
  }

  public static <C> java.util.Collection<C> retainAll(java.util.Collection<C> collection, java.util.Collection<?> retain) {
    return org.apache.commons.collections4.CollectionUtils.retainAll(collection, retain);
  }

  public static <T> boolean filter(java.lang.Iterable<T> collection, org.apache.commons.collections4.Predicate<? super T> predicate) {
    return org.apache.commons.collections4.CollectionUtils.filter(collection, predicate);
  }

  public static <C> void transform(java.util.Collection<C> collection, org.apache.commons.collections4.Transformer<? super C, ? extends C> transformer) {
    org.apache.commons.collections4.CollectionUtils.transform(collection, transformer);
  }

  public static <O, R extends Collection<? super O>> R select(final Iterable<? extends O> inputCollection, final Predicate<? super O> predicate, final R outputCollection, final R rejectedCollection) {
    return org.apache.commons.collections4.CollectionUtils.select(inputCollection, predicate, outputCollection, rejectedCollection);
  }

  public static <O, R extends Collection<? super O>> R select(final Iterable<? extends O> inputCollection, final Predicate<? super O> predicate, final R outputCollection) {
    return org.apache.commons.collections4.CollectionUtils.select(inputCollection, predicate, outputCollection);
  }

  public static <O> Collection<O> select(final Iterable<? extends O> inputCollection, final Predicate<? super O> predicate) {
    return org.apache.commons.collections4.CollectionUtils.select(inputCollection, predicate);
  }

  /**
   * 是否 对象为 null 或 没有元素
   *
   * <ul>
   * <li>Collection - via collection isEmpty
   * <li>Map - via map isEmpty
   * <li>Array - using array size
   * <li>Iterator - via hasNext
   * <li>Enumeration - via hasMoreElements
   * </ul>
   *
   * @param object 对象
   * @return 是否 对象为 null 或 没有元素
   */
  public static boolean sizeIsEmpty(java.lang.Object object) {
    return org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(object);
  }

  /**
   * 是否不满足 集合为 null 或 没有元素
   *
   * @param coll 集合
   * @return 是否不满足 集合为 null 或 没有元素
   */
  public static boolean isNotEmpty(java.util.Collection<?> coll) {
    return org.apache.commons.collections4.CollectionUtils.isNotEmpty(coll);
  }

  public static <E> java.util.Collection<java.util.List<E>> permutations(java.util.Collection<E> collection) {
    return org.apache.commons.collections4.CollectionUtils.permutations(collection);
  }

  public static <E> java.util.Collection<E> transformingCollection(java.util.Collection<E> collection, org.apache.commons.collections4.Transformer<? super E, ? extends E> transformer) {
    return org.apache.commons.collections4.CollectionUtils.transformingCollection(collection, transformer);
  }

  public static <C> java.util.Collection<C> predicatedCollection(java.util.Collection<C> collection, org.apache.commons.collections4.Predicate<? super C> predicate) {
    return org.apache.commons.collections4.CollectionUtils.predicatedCollection(collection, predicate);
  }

  public static boolean isFull(java.util.Collection<?> coll) {
    return org.apache.commons.collections4.CollectionUtils.isFull(coll);
  }

  public static void reverseArray(java.lang.Object[] array) {
    org.apache.commons.collections4.CollectionUtils.reverseArray(array);
  }

  public static int maxSize(java.util.Collection<?> coll) {
    return org.apache.commons.collections4.CollectionUtils.maxSize(coll);
  }

  public static <O> java.util.List<O> collate(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b, java.util.Comparator<? super O> c, boolean includeDuplicates) {
    return org.apache.commons.collections4.CollectionUtils.collate(a, b, c, includeDuplicates);
  }

  public static <O> java.util.List<O> collate(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b, java.util.Comparator<? super O> c) {
    return org.apache.commons.collections4.CollectionUtils.collate(a, b, c);
  }

  public static <O extends Comparable<? super O>> List<O> collate(final Iterable<? extends O> a, final Iterable<? extends O> b) {
    return org.apache.commons.collections4.CollectionUtils.collate(a, b);
  }

  public static <O extends Comparable<? super O>> List<O> collate(final Iterable<? extends O> a, final Iterable<? extends O> b, final boolean includeDuplicates) {
    return org.apache.commons.collections4.CollectionUtils.collate(a, b, includeDuplicates);
  }

  public static <T> java.util.Collection<T> emptyCollection() {
    return org.apache.commons.collections4.CollectionUtils.emptyCollection();
  }

  public static <O> java.util.Collection<O> intersection(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b) {
    return org.apache.commons.collections4.CollectionUtils.intersection(a, b);
  }

  public static <O> java.util.Collection<O> disjunction(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b) {
    return org.apache.commons.collections4.CollectionUtils.disjunction(a, b);
  }

  public static <O> java.util.Collection<O> subtract(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b, org.apache.commons.collections4.Predicate<O> p) {
    return org.apache.commons.collections4.CollectionUtils.subtract(a, b, p);
  }

  public static <O> java.util.Collection<O> subtract(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b) {
    return org.apache.commons.collections4.CollectionUtils.subtract(a, b);
  }

  public static <T> java.util.Collection<T> emptyIfNull(java.util.Collection<T> collection) {
    return org.apache.commons.collections4.CollectionUtils.emptyIfNull(collection);
  }

  public static <O> java.util.Collection<O> union(java.lang.Iterable<? extends O> a, java.lang.Iterable<? extends O> b) {
    return org.apache.commons.collections4.CollectionUtils.union(a, b);
  }

  public static <E> boolean isEqualCollection(java.util.Collection<? extends E> a, java.util.Collection<? extends E> b, org.apache.commons.collections4.Equator<? super E> equator) {
    return org.apache.commons.collections4.CollectionUtils.isEqualCollection(a, b, equator);
  }

  public static boolean isEqualCollection(java.util.Collection<?> a, java.util.Collection<?> b) {
    return org.apache.commons.collections4.CollectionUtils.isEqualCollection(a, b);
  }

  public static <O> java.util.Map<O, java.lang.Integer> getCardinalityMap(java.lang.Iterable<? extends O> coll) {
    return org.apache.commons.collections4.CollectionUtils.getCardinalityMap(coll);
  }

  public static boolean isProperSubCollection(java.util.Collection<?> a, java.util.Collection<?> b) {
    return org.apache.commons.collections4.CollectionUtils.isProperSubCollection(a, b);
  }

  public static boolean containsAny(java.util.Collection<?> coll1, java.util.Collection<?> coll2) {
    return org.apache.commons.collections4.CollectionUtils.containsAny(coll1, coll2);
  }

  public static <T> boolean filterInverse(java.lang.Iterable<T> collection, org.apache.commons.collections4.Predicate<? super T> predicate) {
    return org.apache.commons.collections4.CollectionUtils.filterInverse(collection, predicate);
  }

  public static <O, R extends Collection<? super O>> R selectRejected(final Iterable<? extends O> inputCollection, final Predicate<? super O> predicate, final R outputCollection) {
    return org.apache.commons.collections4.CollectionUtils.selectRejected(inputCollection, predicate, outputCollection);
  }

  public static <O> java.util.Collection<O> selectRejected(java.lang.Iterable<? extends O> inputCollection, org.apache.commons.collections4.Predicate<? super O> predicate) {
    return org.apache.commons.collections4.CollectionUtils.selectRejected(inputCollection, predicate);
  }

  public static <T> boolean addIgnoreNull(java.util.Collection<T> collection, T object) {
    return org.apache.commons.collections4.CollectionUtils.addIgnoreNull(collection, object);
  }

  public static boolean isSubCollection(java.util.Collection<?> a, java.util.Collection<?> b) {
    return org.apache.commons.collections4.CollectionUtils.isSubCollection(a, b);
  }

  public static <E> E extractSingleton(java.util.Collection<E> collection) {
    return org.apache.commons.collections4.CollectionUtils.extractSingleton(collection);
  }

  // endregion

  /**
   * 获取第一个元素
   *
   * @param object 对象
   * @return 第一个元素
   */
  public static Object getFirst(Object object) {
    return get(object, 0);
  }

  /**
   * 获取最后一个元素
   *
   * @param object 对象
   * @return 最后一个元素
   */
  public static Object getLast(Object object) {
    return get(object, size(object) - 1);
  }

  /**
   * 是否 每个集合 都为 null 或 没有元素
   *
   * @param colls 多个集合，只传一个数组请调用 {@link #isEmpty(Collection)}
   * @return 是否 每个集合都 (为 null 或 没有元素)
   */
  public static boolean isEmptys(final Collection<?>... colls) {
    if (colls == null) {
      return true;
    }
    for (Collection<?> coll : colls) {
      // 如果 某个集合 不为 null 且 有元素
      if (!isEmpty(coll)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 是否不满足 每个集合 都为 null 或 没有元素
   *
   * @param colls 多个集合，只传一个数组请调用 {@link #isNotEmpty(Collection)}
   * @return 是否不满足 每个集合 都为 null 或 没有元素
   */
  public static boolean isNotEmptys(final Collection<?>... colls) {
    return !isEmptys(colls);
  }

  /**
   * 是否不满足 对象为 null 或 没有元素
   *
   * @param object 对象
   * @return 是否不满足 对象为 null 或 没有元素
   */
  public static boolean sizeIsNotEmpty(final Object object) {
    return !sizeIsEmpty(object);
  }

  /**
   * 是否 每个对象 都为 null 或 没有元素
   *
   * <ul>
   * <li>Collection - via collection isEmpty
   * <li>Map - via map isEmpty
   * <li>Array - using array size
   * <li>Iterator - via hasNext
   * <li>Enumeration - via hasMoreElements
   * </ul>
   *
   * @param objects 多个对象。只传一个数组请调用 {@link #sizeIsEmpty(Object)}
   * @return 是否 每个对象 都为 null 或 没有元素
   */
  public static boolean sizeIsEmptys(final Object... objects) {
    if (objects == null) {
      return true;
    }
    for (Object object : objects) {
      // 如果 某个对象 不为 null 且 有元素
      if (!sizeIsEmpty(object)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 是否不满足 每个对象 都为 null 或 没有元素
   *
   * @param objects 多个对象。只传一个数组请调用 {@link #sizeIsNotEmpty(Object)}
   * @return 是否不满足 每个对象 都为 null 或 没有元素
   */
  public static boolean sizeIsNotEmptys(final Object... objects) {
    return !sizeIsEmptys(objects);
  }

  /**
   * 是否 对象所有元素都为 null 或 没有元素
   * <p>
   * 注意：会遍历 Iterator，后续使用需重新创建，但是和 {@link #isAllEquals(boolean, Function, Object...)}、{@link #isAllEqualsSameIndex(boolean, Function, Object...)} 使用时却无须担心，因为其内部会在调用此方法前就将 Iterator 转换为 List
   *
   * <ul>
   * <li>Collection - removeIf null, size() == 0
   * <li>Map - self(values())
   * <li>Array - noneMatch nonNull
   * <li>Iterator - !(next() != null)
   * <li>Enumeration - 同 Iterator
   * </ul>
   *
   * @param object 对象
   * @return 是否 对象所有元素都为 null 或 没有元素
   */
  public static boolean isAllEmpty(final Object object) {
    if (sizeIsEmpty(object)) {
      return true;
    }
    if (object instanceof Iterable<?>) {
      for (Object o : (Iterable<?>) object) {
        if (o != null) {
          return false;
        }
      }
    } else if (object instanceof Iterator<?>) {
      Iterator<?> obj1 = (Iterator<?>) object;
      while (obj1.hasNext()) {
        if (obj1.next() != null) {
          return false;
        }
      }
    } else if (object instanceof Map<?, ?>) {
      return isAllEmpty(((Map<?, ?>) object).values());
    } else if (object instanceof Object[]) {
      Object[] obj1 = (Object[]) object;
      return Arrays.stream(obj1).noneMatch(Objects::nonNull);
    } else if (object instanceof Enumeration<?>) {
      Enumeration<?> obj1 = (Enumeration<?>) object;
      while (obj1.hasMoreElements()) {
        if (obj1.nextElement() != null) {
          return false;
        }
      }
    } else {
      throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    }
    return true;
  }

  /**
   * 是否不满足 对象所有元素都为 null 或 没有元素
   *
   * @param object 对象
   * @return 是否不满足 对象所有元素都为 null 或 没有元素
   */
  public static boolean isNotAllEmpty(final Object object) {
    return !isAllEmpty(object);
  }

  /**
   * 是否 每个对象的 所有元素都为 null 或 没有元素
   * <p>
   * 注意：会遍历 Iterator，后续使用需重新创建，但是和 {@link #isAllEquals(boolean, Function, Object...)}、{@link #isAllEqualsSameIndex(boolean, Function, Object...)} 使用时却无须担心，因为其内部会在调用此方法前就将 Iterator 转换为 List
   *
   * <ul>
   * <li>Collection - removeIf null, size() == 0
   * <li>Map - self(values())
   * <li>Array - noneMatch nonNull
   * <li>Iterator - !(next() != null)
   * <li>Enumeration - 同 Iterator
   * </ul>
   *
   * @param objects 多个对象，只传一个数组请调用 {@link #isAllEmpty(Object)}
   * @return 是否 每个对象的 所有元素都为 null 或 没有元素
   */
  public static boolean isAllEmptys(final Object... objects) {
    if (objects == null) {
      return true;
    }
    for (Object object : objects) {
      if (!isAllEmpty(object)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 是否不满足 每个对象的 所有元素都为 null 或 没有元素
   *
   * @param objects 多个对象，只传一个数组请调用 {@link #isNotAllEmpty(Object)}
   * @return 是否不满足 每个对象的 所有元素都为 null 或 没有元素
   */
  public static boolean isNotAllEmptys(final Object... objects) {
    return !isAllEmptys(objects);
  }

  /**
   * 是否 任意对象为 null 或 没有元素 或 任意元素为 null
   * <p>
   * 注意：会遍历 Iterator，后续使用需重新创建，但是和 {@link #isAllEquals(boolean, Function, Object...)}、{@link #isAllEqualsSameIndex(boolean, Function, Object...)} 使用时却无须担心，因为其内部会在调用此方法前就将 Iterator 转换为 List
   *
   * <ul>
   * <li>Collection - contains null
   * <li>Map - containsValue null
   * <li>Array - anyMatch null
   * <li>Iterator - next() == null
   * <li>Enumeration - 同 Iterator
   * </ul>
   *
   * @param object 对象
   * @return 是否 任意对象为 null 或 没有元素 或 任意元素为 null
   */
  public static boolean isAnyEmpty(final Object object) {
    if (sizeIsEmpty(object)) {
      return true;
    } else if (object instanceof Iterable<?>) {
      for (Object o : (Iterable<?>) object) {
        if (o == null) {
          return true;
        }
      }
    } else if (object instanceof Iterator<?>) {
      Iterator<?> obj1 = (Iterator<?>) object;
      while (obj1.hasNext()) {
        if (obj1.next() == null) {
          return true;
        }
      }
    } else if (object instanceof Map<?, ?>) {
      return ((Map<?, ?>) object).containsValue(null);
    } else if (object instanceof Object[]) {
      Object[] obj1 = (Object[]) object;
      return Arrays.stream(obj1).anyMatch(Objects::isNull);
    } else if (object instanceof Enumeration<?>) {
      Enumeration<?> obj1 = (Enumeration<?>) object;
      while (obj1.hasMoreElements()) {
        if (obj1.nextElement() == null) {
          return true;
        }
      }
    } else {
      throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    }
    return false;
  }

  /**
   * 是否不满足 任意对象为 null 或 没有元素 或 任意元素为 null
   *
   * @param object 对象
   * @return 是否不满足 任意对象为 null 或 没有元素 或 任意元素为 null
   */
  public static boolean isNotAnyEmpty(final Object object) {
    return !isAnyEmpty(object);
  }

  /**
   * 是否 任意对象 为 null 或 没有元素 或 任意元素为 null
   * <p>
   * 注意：会遍历 Iterator，后续使用需重新创建，但是和 {@link #isAllEquals(boolean, Function, Object...)}、{@link #isAllEqualsSameIndex(boolean, Function, Object...)} 使用时却无须担心，因为其内部会在调用此方法前就将 Iterator 转换为 List
   *
   * <ul>
   * <li>Collection - contains null
   * <li>Map - containsValue null
   * <li>Array - anyMatch null
   * <li>Iterator - next() == null
   * <li>Enumeration - 同 Iterator
   * </ul>
   *
   * @param objects 多个对象，只传一个数组请调用 {@link #isAnyEmpty(Object)}
   * @return 是否 任意对象 为 null 或 没有元素 或 任意元素为 null
   */
  public static boolean isAnyEmptys(final Object... objects) {
    if (objects == null) {
      return true;
    }
    for (Object object : objects) {
      if (isAnyEmpty(object)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 是否不满足 任意对象 为 null 或 没有元素 或 任意元素为 null
   *
   * @param objects 多个对象，只传一个数组请调用 {@link #isNotAnyEmpty(Object)}
   * @return 是否不满足 任意对象 为 null 或 没有元素 或 任意元素为 null
   */
  public static boolean isNotAnyEmptys(final Object... objects) {
    return !isAnyEmptys(objects);
  }

  /**
   * 转为字符串，Number 小数点后多余的 0 会被去除
   *
   * @param object     需转换的对象
   * @param isToString 是否转换
   * @return 字符串
   */
  private static Object stripTrailingZerosToString(Object object, final boolean isToString) {
    if (isToString) {
      if (object instanceof Number) {
        object = new BigDecimal(object.toString()).stripTrailingZeros().toPlainString();
      } else {
        object = object.toString();
      }
    }
    return object;
  }

  /**
   * 是否 每个对象的每个元素都相等
   *
   * @param isToString       是否根据 toString() 的值来判断是否相等，
   *                         Number 会去除小数点后多余的 0。
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否 每个对象的每个元素都相等
   */
  public static boolean isAllEquals(final boolean isToString, final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    if (objects.length < 2) {
      throw new IllegalArgumentException("Objects: length must be greater than 1.");
    }

    Object prevObj = null;
    // 是否已分配 prevObj，因为虽然根据 prevObj == null 判断了是否为第一次循环，但有可能第一次循环时 prevObj 仍赋值为 null
    boolean isAssigned = false;
    for (int i = 0; i < objects.length; i++) {
      Object object = objects[i];

      // 满足条件时对象不参与判断
      if (continueFunction != null) {
        // Iterator、Enumeration 在 continueFunction 可能需要循环，所以先转换为 List，避免后续无法循环
        if (object instanceof Iterator<?>) {
          object = IteratorUtils.toList((Iterator<?>) object);
          objects[i] = object;
        } else if (object instanceof Enumeration<?>) {
          object = EnumerationUtils.toList((Enumeration<?>) object);
          objects[i] = object;
        }

        if (Boolean.TRUE.equals(continueFunction.apply(object))) {
          continue;
        }
      }

      if (object instanceof Iterable<?> || object instanceof Iterator<?> || object instanceof Map<?, ?>) {
        Iterator<?> iterator;
        if (object instanceof Iterable<?>) {
          iterator = ((Iterable<?>) object).iterator();
        } else if (object instanceof Map<?, ?>) {
          iterator = (((Map<?, ?>) object).values()).iterator();
        } else {
          iterator = (Iterator<?>) object;
        }

        while (iterator.hasNext()) {
          Object nextObj = iterator.next();
          // 如果是 JSON 元素则获取其值
          nextObj = JsonReflectUtil.getValue(nextObj);
          // 转为字符串，Number 小数点后多余的 0 会被去除
          nextObj = stripTrailingZerosToString(nextObj, isToString);
          // 首次判断前跳过第一次循环
          if (prevObj == null && !isAssigned) {
            // 上一次元素赋值
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof Object[]) {
        Object[] array = (Object[]) object;
        for (Object o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof int[]) {
        int[] array = (int[]) object;
        for (int o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof long[]) {
        long[] array = (long[]) object;
        for (long o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof double[]) {
        double[] array = (double[]) object;
        for (double o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof float[]) {
        float[] array = (float[]) object;
        for (float o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof char[]) {
        char[] array = (char[]) object;
        for (char o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof byte[]) {
        byte[] array = (byte[]) object;
        for (byte o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof boolean[]) {
        boolean[] array = (boolean[]) object;
        for (boolean o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof short[]) {
        short[] array = (short[]) object;
        for (short o : array) {
          Object nextObj = stripTrailingZerosToString(o, isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else if (object instanceof Enumeration<?>) {
        Enumeration<?> enumeration = (Enumeration<?>) object;
        while (enumeration.hasMoreElements()) {
          Object nextObj = stripTrailingZerosToString(enumeration.nextElement(), isToString);
          if (prevObj == null && !isAssigned) {
            prevObj = nextObj;
            isAssigned = true;
            continue;
          }
          if (!Objects.equals(prevObj, nextObj)) {
            return false;
          }
        }
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * 是否 每个对象的每个元素都相等
   * <p>
   * 会根据 toString() 的值来判断是否相等，Number 会去除小数点后多余的 0。
   *
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否 每个对象的每个元素都相等
   */
  public static boolean isAllEquals(final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return isAllEquals(true, continueFunction, objects);
  }

  /**
   * 是否不满足 每个对象的每个元素都相等
   *
   * @param isToString       是否根据 toString() 的值来判断是否相等，
   *                         Number 会去除小数点后多余的 0。
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否不满足 每个对象的每个元素都相等
   */
  public static boolean isNotAllEquals(final boolean isToString, final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return !isAllEquals(isToString, continueFunction, objects);
  }

  /**
   * 是否不满足 每个对象的每个元素都相等
   * <p>
   * 会根据 toString() 的值来判断是否相等，Number 会去除小数点后多余的 0。
   *
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否不满足 每个对象的每个元素都相等
   */
  public static boolean isNotAllEquals(final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return isNotAllEquals(true, continueFunction, objects);
  }

  /**
   * 是否 每个对象的同一位置的元素都相等
   *
   * @param isToString       是否根据 toString() 的值来判断是否相等，
   *                         Number 会去除小数点后多余的 0。
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否 每个对象的同一位置的元素都相等
   */
  public static boolean isAllEqualsSameIndex(final boolean isToString, final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    if (objects.length < 2) {
      throw new IllegalArgumentException("Objects: length must be greater than 1.");
    }

    Integer prevObjSize = null;
    for (int i = 0; i < objects.length; i++) {
      Object object = objects[i];

      // Iterator、Enumeration 在 continueFunction 或获取对象元素长度时可能需要循环，所以先转换为 List，避免后续无法循环
      if (object instanceof Iterator<?>) {
        object = IteratorUtils.toList((Iterator<?>) object);
        objects[i] = object;
      } else if (object instanceof Enumeration<?>) {
        object = EnumerationUtils.toList((Enumeration<?>) object);
        objects[i] = object;
      }
      // 满足条件时对象不参与判断
      if (continueFunction != null && Boolean.TRUE.equals(continueFunction.apply(object))) {
        continue;
      }

      int objSize = -1;
      // 不同类型获取元素长度
      if (object instanceof Iterable<?>) {
        if (object instanceof Collection<?>) {
          objSize = ((Collection<?>) object).size();
        } else {
          int j = 0;
          for (Object ignored : (Iterable<?>) object) {
            j++;
          }
          objSize = j;
        }
      } else if (object instanceof Map<?, ?>) {
        objSize = ((Map<?, ?>) object).values().size();
      } else if (object != null && object.getClass().isArray()) {
        objSize = Array.getLength(object);
      } else {
        return false;
      }
      // 当前对象元素长度和上一次循环的对象元素长度不一致时退出
      if (prevObjSize != null && !prevObjSize.equals(objSize)) {
        return false;
      }
      prevObjSize = objSize;
    }

    List<Object> prevList = new LinkedList<>();
    for (Object object : objects) {
      // 满足条件时对象不参与判断
      if (continueFunction != null && Boolean.TRUE.equals(continueFunction.apply(object))) {
        continue;
      }

      // Iterable、Map
      if (object instanceof Iterable<?> || object instanceof Map<?, ?>) {
        // 不同类型获取 Iterator
        Iterator<?> iterator;
        if (object instanceof Iterable<?>) {
          iterator = ((Iterable<?>) object).iterator();
        } else {
          iterator = (((Map<?, ?>) object).values()).iterator();
        }

        for (int i = 0; iterator.hasNext(); i++) {
          Object nextObj = iterator.next();
          // 如果是 JSON 元素则获取其值
          nextObj = JsonReflectUtil.getValue(nextObj);
          // 转为字符串，Number 小数点后多余的 0 会被去除
          nextObj = stripTrailingZerosToString(nextObj, isToString);
          // 如果上一次列表的长度 < i + 1，即 prevList（上一次列表）未填充完需要判断的第一个对象的所有元素
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          // 对比上一次列表和当前列表的元素
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      }
      // Object[]
      else if (object instanceof Object[]) {
        Object[] array = (Object[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof int[]) {
        int[] array = (int[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof long[]) {
        long[] array = (long[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof double[]) {
        double[] array = (double[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof float[]) {
        float[] array = (float[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof char[]) {
        char[] array = (char[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof byte[]) {
        byte[] array = (byte[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof boolean[]) {
        boolean[] array = (boolean[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      } else if (object instanceof short[]) {
        short[] array = (short[]) object;
        for (int i = 0; i < array.length; i++) {
          Object nextObj = stripTrailingZerosToString(array[i], isToString);
          if (prevList.size() < i + 1) {
            prevList.add(nextObj);
            continue;
          }
          if (!Objects.equals(prevList.get(i), nextObj)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * 是否 每个对象的同一位置的元素都相等
   * <p>
   * 会根据 toString() 的值来判断是否相等，Number 会去除小数点后多余的 0。
   *
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否 每个对象的同一位置的元素都相等
   */
  public static boolean isAllEqualsSameIndex(final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return isAllEqualsSameIndex(true, continueFunction, objects);
  }

  /**
   * 是否不满足 每个对象的同一位置的元素都相等
   *
   * @param isToString       是否根据 toString() 的值来判断是否相等，
   *                         Number 会去除小数点后多余的 0。
   * @param continueFunction 对象何时不参与判断
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否不满足 每个对象的同一位置的元素都相等
   */
  public static boolean isNotAllEqualsSameIndex(final boolean isToString, final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return !isAllEqualsSameIndex(isToString, continueFunction, objects);
  }

  /**
   * 是否不满足 每个对象的同一位置的元素都相等
   * <p>
   * 会根据 toString() 的值来判断是否相等，Number 会去除小数点后多余的 0。
   *
   * @param continueFunction 对象何时不参与判断。Iterator、Enumeration 会在判断前转换为 List。
   * @param objects          多个对象。Iterator、Enumeration 会被使用掉。
   * @return 是否不满足 每个对象的同一位置的元素都相等
   */
  public static boolean isNotAllEqualsSameIndex(final Function<Object, Boolean> continueFunction, @NonNull final Object... objects) {
    return isNotAllEqualsSameIndex(true, continueFunction, objects);
  }

  /**
   * 集合是否包含指定元素
   *
   * @param c 是否包含元素的集合
   * @param o 集合是否包含的元素
   * @return {@code true} 如果集合包含指定元素
   */
  public static boolean contains(Collection<?> c, Object o) {
    if (CollUtil.isEmpty(c)) {
      return false;
    }
    return c.contains(o);
  }
}
