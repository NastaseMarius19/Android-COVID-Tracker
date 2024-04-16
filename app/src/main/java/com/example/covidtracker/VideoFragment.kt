import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import com.example.covidtracker.R

class VideoFragment : Fragment() {
    private lateinit var videoView: VideoView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                R.layout.fragment_video_landscape
            } else {
                R.layout.fragment_video
            },
            container,
            false
        )
        videoView = view.findViewById(R.id.video_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoPath = "android.resource://${requireActivity().packageName}/${R.raw.video}"
        val videoUri = Uri.parse(videoPath)

        videoView.setVideoURI(videoUri)

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.start()
    }
}
