
# AutoScrollableRecyclerView

A small library that enables auto-scrolling of `RecyclerView` items and displays the viewing progress. The library offers flexibility in configuring the auto-scrolling process (such as its duration) and the segmented progress bar parameters (layout params, gravity, margins, colors, distance between segments, etc.). If you don't need this functionality, you can use `AutoScrollableRecyclerView` just like a common `RecyclerView` 😊


## Features

- Auto-scrolling of `RecyclerView` list
- Displaying of viewing progress
- Manual parameter configuration via XML


## Library constraints
- **Works only with linear lists (`LayoutManager` implemented as `LinearLayoutManager`)**
- **`RecyclerView` adapter must extend `ListAdapter`**
- **`SegmentedLinearProgressBar` is always horizontal**
## Advantages
- Is really lightweight
- Uses modern technologies following the principle of necessity and sufficiency:
  -  _`RecyclerView` for list management_
  -  _Kotlin Coroutines for auto-scrolling logic_
  -  _`ValueAnimator` for displaying progress_
- Has only 1 third-party dependency "com.google.android.material:1.12.0"
## Requirements
- Android compileSdk version: **35**
- Android minSdk version: **24**
- Kotlin version: **2.1.21**
- Java version: **17**
## Installation

Implement library easily:

```bash
  implementation 'io.github.crabster87:autoscrollable-rv-segmented-progress:1.0.1'
```
Add `AutoScrollableRecyclerView` to your layout:    
```
<io.github.crabster87.autoscrollablerecyclerview.AutoScrollableRecyclerView
        android:id="@+id/auto_scrollable_rv"
        android:layout_width="your preference"
        android:layout_height="your preference"
        app:recyclerViewOrientation="your preference" />
```
## Configuring XML

Special XML parameters for detailed configuration of the autoscrolling logic and progress bar behavior, including its position and appearance:

| _Parameter_ | _Type_     | _Default Value_                |
| :-------- | :------- | :------------------------- |
| `displayingDuration` | `Int` | 0 |
| `isAutoScrollable` | `Boolean` | true |
| `recyclerViewOrientation` | `Int` | LinearLayoutManager.HORIZONTAL |
| `progressLayoutWidth` | `Dimension` | wrap_content |
| `progressLayoutHeight` | `Dimension` | wrap_content |
| `progressLayoutGravity` | `Int` | no_gravity |
| `progressMinValue` | `Int` | 0 |
| `progressMaxValue` | `Int` | 100 |
| `progressSpacing` | `Dimension` | 0 dp |
| `progressBackgroundColor` | `Color` | Color.GRAY |
| `progressColor` | `Color` | Color.BLUE |
| `progressCornerRadius` | `Dimension` | 0 dp |
| `progressAlpha` | `Float` | 1f |
| `progressLayoutMarginTop` | `Dimension` | 0 dp |
| `progressLayoutMarginBottom` | `Dimension` | 0 dp |
| `progressLayoutMarginStart` | `Dimension` | 0 dp |
| `progressLayoutMarginEnd` | `Dimension` | 0 dp |


## Configuring logic in Fragment
#### 1) Set adapter and scroll listener
```
private var adapter: ListAdapter? = null
...
override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        adapter = ListAdapter()
        autoScrollableRv.apply {
            setAdapter(adapter)
            // Call this method if you want to handle user's scroll gestures
            setOnScrollListener(viewLifecycleOwner)
        }
...
}
```
#### 2) Fetch data from ViewModel and set it to adapter
```
override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
...        
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.items.collectLatest {
                    autoScrollableRv.apply {
                        submitData(adapter, it, viewLifecycleOwner)
                        // Call this method if you want to display SegmentedLinearProgressBar
                        displaySegmentedLinearProgressBar(it)
                    }
                }
            }
        }
...        
}
```
#### 3) Manage according to the Fragment lifecycle (examples)
```
    override fun onResume() = with(binding) {
        super.onResume()
        // If you want to restart scrolling after the Fragment becomes visible
        autoScrollableRv.launchAutoScrolling(viewLifecycleOwner)
    }
}
```
```
    override fun onPause() = with(binding) {
        super.onPause()
        // If you want to stop scrolling after the Fragment becomes invisible
        autoScrollableRv.stopAutoScrolling()
    }
}
```
#### 4) Set SegmentedLinearProgressBar margins programmatically
```
override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
...        
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.items.collectLatest {
                autoScrollableRv.apply {
                    submitData(adapter, it, viewLifecycleOwner)
                    // You can define and pass a special object container as the second parameter
                    displaySegmentedLinearProgressBar(it,
                        ProgressBarParams.Margins(
                            progressLayoutMarginTop = 0,
                            progressLayoutMarginBottom = 8,
                            progressLayoutMarginStart = 24,
                            progressLayoutMarginEnd = 24
                        )
                    )
                }
            }
        }
    }
...        
}
```
## Demo

![](assets/autoscrolling_demo.gif)


## 🚀 About Me
I'm an Android developer... [@Crabster87](https://github.com/Crabster87)


## 🔗 Links
[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/alexey-rudakov-5684b42a8//)


## Feedback

If you have any feedback, please reach out to us at iar87654@gmail.com

