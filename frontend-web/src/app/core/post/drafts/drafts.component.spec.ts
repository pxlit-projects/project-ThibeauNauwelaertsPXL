import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DraftPostsComponent } from './drafts.component';
import { PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Post } from '../../../shared/models/post.model';
import { NotificationMessage } from '../../../shared/models/notification-message.model';
import { NgZone } from '@angular/core';

describe('DraftPostsComponent', () => {
  let component: DraftPostsComponent;
  let fixture: ComponentFixture<DraftPostsComponent>;
  let postServiceMock: jasmine.SpyObj<PostService>;
  let routerMock: jasmine.SpyObj<Router>;
  let ngZone: NgZone;

  const mockDrafts: Post[] = [
    { id: 1, title: 'Draft 1', content: '...', author: 'someone', status: 'DRAFT' },
    { id: 2, title: 'Draft 2', content: '...', author: 'someone', status: 'DRAFT' },
  ];

  beforeEach(async () => {
    postServiceMock = jasmine.createSpyObj<PostService>('PostService', [
      'getDraftPosts',
      'updatePost',
    ]);
    routerMock = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [DraftPostsComponent],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        { provide: Router, useValue: routerMock },
        // Do NOT provide NgZone directly; Angular does it automatically.
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DraftPostsComponent);
    component = fixture.componentInstance;
    ngZone = TestBed.inject(NgZone); // You can still inject it here if needed
  });

  describe('Initialization', () => {
    it('should create the component', () => {
      postServiceMock.getDraftPosts.and.returnValue(of(mockDrafts));

      fixture.detectChanges(); // triggers ngOnInit

      expect(component).toBeTruthy();
    });
  });

    it('should fetch draft posts on ngOnInit', () => {
      spyOn(component, 'fetchDraftPosts').and.callThrough();

      postServiceMock.getDraftPosts.and.returnValue(of(mockDrafts));

      fixture.detectChanges(); // triggers ngOnInit

      expect(component.fetchDraftPosts).toHaveBeenCalled();
      expect(postServiceMock.getDraftPosts).toHaveBeenCalled();
    });

  describe('fetchDraftPosts', () => {
    it('should set draftPosts on success', () => {
      postServiceMock.getDraftPosts.and.returnValue(of(mockDrafts));

      component.fetchDraftPosts();

      expect(component.draftPosts).toEqual(mockDrafts);
      expect(component.errorMessage).toBeNull();
      expect(component.loading).toBeFalse();
    });
  });
  
  describe('applyFilters', () => {
    it('should call fetchDraftPosts with filtered criteria', () => {
      spyOn(component, 'fetchDraftPosts');

      component.filterCriteria.title = 'Draft 1';
      component.applyFilters();

      expect(component.fetchDraftPosts).toHaveBeenCalledWith({ title: 'Draft 1' });
    });
  });

  describe('clearFilters', () => {
    it('should clear filterCriteria and refetch posts', () => {
      spyOn(component, 'fetchDraftPosts');

      component.filterCriteria = { title: 'Test' };
      component.clearFilters();

      expect(component.filterCriteria).toEqual({});
      expect(component.fetchDraftPosts).toHaveBeenCalled();
    });
  });

  describe('addNewDraft', () => {
    it('should push a new draft to draftPosts array', () => {
      const newDraft: Post = { id: 3, title: 'Draft 3', content: '...', author: 'new-author', status: 'DRAFT' };
      component.addNewDraft(newDraft);

      expect(component.draftPosts[component.draftPosts.length - 1]).toBe(newDraft);
    });
  });

  describe('navigateToCreatePost', () => {
    it('should call router.navigate to create-post', () => {
      component.navigateToCreatePost();
      expect(routerMock.navigate).toHaveBeenCalledWith(['/create-post']);
    });
  });

  describe('editPost', () => {
    it('should navigate to edit-post with the postId', () => {
      component.editPost(1);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/edit-post', 1]);
    });
  });
});
